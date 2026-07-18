package com.portfolio.banking_api.domain;

import com.portfolio.banking_api.exception.BadRequestException;
import com.portfolio.banking_api.exception.InsufficientFundsException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

class AccountTest {
    @Test
    void constructor_setAllFields() {
        UUID userId = UUID.randomUUID();
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );
        assertNotNull(account.getId());
        assertEquals("1234567890", account.getAccountNumber());
        assertEquals("Alex Morgan", account.getOwnerName());
        assertEquals(userId, account.getUserId());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals("USD", account.getCurrency());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void noArgConstructor_canBeInvoked() {
        Account account = new Account();
        assertNull(account.getId());
        assertNull(account.getAccountNumber());
        assertNull(account.getOwnerName());
        assertNull(account.getUserId());
        assertNull(account.getBalance());
        assertNull(account.getCurrency());
        assertNull(account.getCreatedAt());
    }

    @Test
    void credit_increasesBalance() {
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            UUID.randomUUID(),
            new BigDecimal("1000.00"),
            "USD"
        );
        account.credit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("1100.00"), account.getBalance());
    }

    @Test
    void credit_rejectsNonPositiveAmount() {
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            UUID.randomUUID(),
            new BigDecimal("1000.00"),
            "USD"
        );
        assertThrows(BadRequestException.class, () -> account.credit(new BigDecimal("0.00")));
    }

    @Test
    void debit_decreasesBalance() {
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            UUID.randomUUID(),
            new BigDecimal("1000.00"),
            "USD"
        );
        account.debit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("900.00"), account.getBalance());
    }

    @Test
    void debit_rejectsNonPositiveAmount() {
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            UUID.randomUUID(),
            new BigDecimal("1000.00"),
            "USD"
        );
        assertThrows(BadRequestException.class, () -> account.debit(new BigDecimal("0.00")));
    }

    @Test
    void debit_rejectsInsufficientFunds() {
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            UUID.randomUUID(),
            new BigDecimal("1000.00"),
            "USD"
        );
        assertThrows(InsufficientFundsException.class, () -> account.debit(new BigDecimal("1100.00")));
    }
}