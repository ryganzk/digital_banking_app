package com.portfolio.banking_api.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

class TransactionTest {
    @Test
    void constructor_setAllFields() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        UUID counterpartyAccountId = UUID.randomUUID();
        UUID transferGroupId = UUID.randomUUID();
        Transaction transaction = new Transaction(
            accountId,
            TransactionType.TRANSFER_OUT,
            amount,
            "USD",
            counterpartyAccountId,
            transferGroupId
        );
        assertNotNull(transaction.getId());
        assertEquals(accountId, transaction.getAccountId());
        assertEquals(TransactionType.TRANSFER_OUT, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertEquals(counterpartyAccountId, transaction.getCounterpartyAccountId());
        assertEquals(transferGroupId, transaction.getTransferGroupId());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void constructor_allowedNullCounterpartyAndTransferGroup() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        Transaction transaction = new Transaction(
            accountId,
            TransactionType.DEPOSIT,
            amount,
            "USD",
            null,
            null
        );
        assertNotNull(transaction.getId());
        assertEquals(accountId, transaction.getAccountId());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertNull(transaction.getCounterpartyAccountId());
        assertNull(transaction.getTransferGroupId());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void noArgConstructor_canBeInvoked() {
        Transaction transaction = new Transaction();
        assertNull(transaction.getId());
        assertNull(transaction.getAccountId());
        assertNull(transaction.getType());
        assertNull(transaction.getAmount());
        assertNull(transaction.getCurrency());
        assertNull(transaction.getCounterpartyAccountId());
        assertNull(transaction.getTransferGroupId());
        assertNull(transaction.getCreatedAt());
    }
}