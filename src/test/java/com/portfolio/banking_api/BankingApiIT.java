package com.portfolio.banking_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class BankingApiIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("banking_db")
        .withUsername("banking")
        .withPassword("banking");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerCreateAccountsTransfer_updatesBalancesAndLedger() throws Exception {
        String token = registerAndGetToken();

        String sourceId = createAccount(token, "ACC-001", "Alex Morgan", "1000.00");
        createAccount(token, "ACC-002", "Alex Morgan", "100.00");

        mockMvc.perform(post("/api/v1/accounts/" + sourceId + "/transfer")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 50.00,
                    "toAccountNumber": "ACC-002"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("TRANSFER_OUT"))
            .andExpect(jsonPath("$.amount").value(50.00))
            .andExpect(jsonPath("$.transferGroupId").isNotEmpty());

        MvcResult accountsResult = mockMvc.perform(get("/api/v1/accounts")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode accounts = objectMapper.readTree(accountsResult.getResponse().getContentAsString());
        assertEquals(0, balanceFor(accounts, "ACC-001").compareTo(new BigDecimal("950.00")));
        assertEquals(0, balanceFor(accounts, "ACC-002").compareTo(new BigDecimal("150.00")));
    }

    private String registerAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "alex@example.com",
                    "password": "password123",
                    "firstName": "Alex",
                    "middleName": "",
                    "lastName": "Morgan"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token")
            .asText();
    }

    private String createAccount(String token, String accountNumber, String ownerName, String initialBalance) throws Exception {
        String payload = """
            {
                "accountNumber": "%s",
                "ownerName": "%s",
                "initialBalance": %s,
                "currency": "USD"
            }
            """.formatted(accountNumber, ownerName, initialBalance);

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.accountNumber").value(accountNumber))
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id")
            .asText();
    }

    private BigDecimal balanceFor(JsonNode accounts, String accountNumber) {
        return StreamSupport.stream(accounts.spliterator(), false)
            .filter(account -> accountNumber.equals(account.get("accountNumber").asText()))
            .map(account -> new BigDecimal(account.get("balance").asText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Account not found: " + accountNumber));
    }
}
