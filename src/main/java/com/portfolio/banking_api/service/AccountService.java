package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.Account;
import com.portfolio.banking_api.domain.Transaction;
import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.CreateAccountRequest;
import com.portfolio.banking_api.repository.AccountRepository;
import com.portfolio.banking_api.dto.MoneyRequest;
import com.portfolio.banking_api.dto.TransferRequest;
import com.portfolio.banking_api.dto.TransactionResponse;
import com.portfolio.banking_api.domain.TransactionType;
import com.portfolio.banking_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, UUID userId) {
        Account account = new Account(
            request.accountNumber(),
            request.ownerName(),
            userId,
            request.initialBalance(),
            request.currency()
        );
        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse deposit(UUID accountId, UUID userId, MoneyRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new NoSuchElementException("Account not found"));
        account.credit(request.amount());
        accountRepository.save(account);

        Transaction transaction = new Transaction(
            account.getId(),
            TransactionType.DEPOSIT,
            request.amount(),
            account.getCurrency(),
            null,
            null
        );
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(UUID accountId, UUID userId, MoneyRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new NoSuchElementException("Account not found"));
        account.debit(request.amount());
        accountRepository.save(account);

        Transaction transaction = new Transaction(
            account.getId(),
            TransactionType.WITHDRAWAL,
            request.amount(),
            account.getCurrency(),
            null,
            null
        );
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(UUID accountId, UUID userId, TransferRequest request) {
        Account sourceAccount = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new NoSuchElementException("Account not found"));
        Account destinationAccount = accountRepository.findByAccountNumber(request.toAccountNumber())
            .orElseThrow(() -> new NoSuchElementException("Destination account not found"));

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }
        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        UUID transferGroupId = UUID.randomUUID();
        sourceAccount.debit(request.amount());
        destinationAccount.credit(request.amount());
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transferOut = new Transaction(
            sourceAccount.getId(),
            TransactionType.TRANSFER_OUT,
            request.amount(),
            sourceAccount.getCurrency(),
            destinationAccount.getId(),
            transferGroupId
        );
        Transaction transferIn = new Transaction(
            destinationAccount.getId(),
            TransactionType.TRANSFER_IN,
            request.amount(),
            destinationAccount.getCurrency(),
            sourceAccount.getId(),
            transferGroupId
        );

        transactionRepository.save(transferOut);
        transactionRepository.save(transferIn);
        return toTransactionResponse(transferOut);
    }

    public List<AccountResponse> getAllAccountsByUserId(UUID userId) {
        return accountRepository.findAllByUserId(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getOwnerName(),
            account.getUserId(),
            account.getBalance(),
            account.getCurrency(),
            account.getCreatedAt()
        );
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAccountId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getCounterpartyAccountId(),
            transaction.getTransferGroupId(),
            transaction.getCreatedAt()
        );
    }
}