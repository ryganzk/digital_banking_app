package com.portfolio.banking_api.controller;

import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.service.AccountService;
import com.portfolio.banking_api.service.JWTService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JWTService jwtService;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void authenticateUser() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAccount_returns201() throws Exception {
        when(accountService.createAccount(any(), eq(userId))).thenReturn(
            new AccountResponse(
            UUID.randomUUID(),
            "ACC-001",
            "Alex",
            userId,
            new BigDecimal("1000.00"),
            "USD",
            Instant.now()
        ));

        mockMvc.perform(post("/api/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountNumber": "ACC-001",
                    "ownerName": "Alex",
                    "initialBalance": 1000.00,
                    "currency": "USD"
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountNumber").value("ACC-001"));
    }

    @Test
    void getMyAccounts_returns200() throws Exception {
        when(accountService.getAllAccountsByUserId(userId)).thenReturn(List.of(
            new AccountResponse(
                UUID.randomUUID(),
                "ACC-001",
                "Alex",
                userId,
                new BigDecimal("1000.00"),
                "USD",
                Instant.now()
            )
        ));

        mockMvc.perform(get("/api/v1/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].accountNumber").value("ACC-001"));
    }
}
