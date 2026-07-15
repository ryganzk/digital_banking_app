package com.portfolio.banking_api.controller;

import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.CreateAccountRequest;
import com.portfolio.banking_api.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        UUID userId = UUID.fromString(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        );
        return accountService.createAccount(request, userId);
    }

    @GetMapping
    public List<AccountResponse> getMyAccounts() {
        UUID userId = UUID.fromString(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        );
        return accountService.getAllAccountsByUserId(userId);
    }
}