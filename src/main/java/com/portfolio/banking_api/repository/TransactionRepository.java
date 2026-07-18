package com.portfolio.banking_api.repository;

import com.portfolio.banking_api.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByAccountId(UUID accountId);
    List<Transaction> findAllByTransferGroupId(UUID transferGroupId);
}