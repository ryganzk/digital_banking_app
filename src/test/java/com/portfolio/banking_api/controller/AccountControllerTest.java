package com.portfolio.banking_api.controller;

import com.portfolio.banking_api.domain.TransactionType;
import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.TransactionResponse;
import com.portfolio.banking_api.exception.AccountNotFoundException;
import com.portfolio.banking_api.exception.InsufficientFundsException;
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
    private final UUID accountId = UUID.fromString("22222222-2222-2222-2222-222222222222");

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

    @Test
    void deposit_returns200() throws Exception {
        when(accountService.deposit(eq(accountId), eq(userId), any())).thenReturn(
            transactionResponse(TransactionType.DEPOSIT, null, null)
        );

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("DEPOSIT"))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.accountId").value(accountId.toString()));
    }

    @Test
    void deposit_returns404WhenAccountMissing() throws Exception {
        when(accountService.deposit(eq(accountId), eq(userId), any()))
            .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00
                }
                """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void deposit_returns400WhenAmountInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": -1
                }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void withdraw_returns200() throws Exception {
        when(accountService.withdraw(eq(accountId), eq(userId), any())).thenReturn(
            transactionResponse(TransactionType.WITHDRAWAL, null, null)
        );

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
            .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void withdraw_returns404WhenAccountMissing() throws Exception {
        when(accountService.withdraw(eq(accountId), eq(userId), any()))
            .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00
                }
                """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void withdraw_returns400WhenInsufficientFunds() throws Exception {
        when(accountService.withdraw(eq(accountId), eq(userId), any()))
            .thenThrow(new InsufficientFundsException("Insufficient balance"));

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00
                }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INSUFFICIENT_FUNDS"))
            .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void transfer_returns200() throws Exception {
        UUID counterpartyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID transferGroupId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        when(accountService.transfer(eq(accountId), eq(userId), any())).thenReturn(
            transactionResponse(TransactionType.TRANSFER_OUT, counterpartyId, transferGroupId)
        );

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00,
                    "toAccountNumber": "ACC-002"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("TRANSFER_OUT"))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.counterpartyAccountId").value(counterpartyId.toString()));
    }

    @Test
    void transfer_returns404WhenAccountMissing() throws Exception {
        when(accountService.transfer(eq(accountId), eq(userId), any()))
            .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00,
                    "toAccountNumber": "ACC-002"
                }
                """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void transfer_returns400WhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "amount": 100.00
                }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private TransactionResponse transactionResponse(TransactionType type, UUID counterpartyId, UUID transferGroupId) {
        return new TransactionResponse(
            UUID.fromString("55555555-5555-5555-5555-555555555555"),
            accountId,
            type,
            new BigDecimal("100.00"),
            "USD",
            counterpartyId,
            transferGroupId,
            Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
