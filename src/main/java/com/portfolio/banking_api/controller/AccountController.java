package com.portfolio.banking_api.controller;

import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.CreateAccountRequest;
import com.portfolio.banking_api.dto.MoneyRequest;
import com.portfolio.banking_api.dto.TransferRequest;
import com.portfolio.banking_api.dto.TransactionResponse;
import com.portfolio.banking_api.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request, currentUserId());
    }

    @PostMapping("/{accountId}/deposit")
    public TransactionResponse deposit(@PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request) {
        return accountService.deposit(accountId, currentUserId(), request);
    }

    @PostMapping("/{accountId}/withdraw")
    public TransactionResponse withdraw(@PathVariable UUID accountId, @Valid @RequestBody MoneyRequest request) {
        return accountService.withdraw(accountId, currentUserId(), request);
    }

    @PostMapping("/{accountId}/transfer")
    public TransactionResponse transfer(@PathVariable UUID accountId, @Valid @RequestBody TransferRequest request) {
        return accountService.transfer(accountId, currentUserId(), request);
    }

    @GetMapping
    public List<AccountResponse> getMyAccounts() {
        return accountService.getAllAccountsByUserId(currentUserId());
    }

    private UUID currentUserId() {
        return UUID.fromString(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        );
    }
}