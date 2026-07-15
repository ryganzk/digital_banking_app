package com.portfolio.banking_api.domain;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {
    
    }

    public Account(String accountNumber, String ownerName, BigDecimal balance, String currency) {
        this.id = UUID.randomUUID();
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}