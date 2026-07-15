package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.Account;
import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.CreateAccountRequest;
import com.portfolio.banking_api.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account(
            request.accountNumber(),
            request.ownerName(),
            request.initialBalance(),
            request.currency()
        );
        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getOwnerName(),
            account.getBalance(),
            account.getCurrency(),
            account.getCreatedAt()
        );
    }
}