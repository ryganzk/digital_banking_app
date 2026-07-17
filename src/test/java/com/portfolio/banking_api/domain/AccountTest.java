package com.portfolio.banking_api.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}