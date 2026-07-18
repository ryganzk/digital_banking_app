package com.portfolio.banking_api.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.sql.Types;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @JdbcTypeCode(Types.CHAR)
    private String currency;

    @Column(name = "counterparty_account_id", nullable = true)
    private UUID counterpartyAccountId;

    @Column(name = "transfer_group_id")
    private UUID transferGroupId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {

    }

    public Transaction(UUID accountId, TransactionType type, BigDecimal amount, String currency, UUID counterpartyAccountId, UUID transferGroupId) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.counterpartyAccountId = counterpartyAccountId;
        this.transferGroupId = transferGroupId;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public UUID getCounterpartyAccountId() { return counterpartyAccountId; }
    public UUID getTransferGroupId() { return transferGroupId; }
    public Instant getCreatedAt() { return createdAt; }
}