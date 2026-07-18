package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.Account;
import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.CreateAccountRequest;
import com.portfolio.banking_api.repository.AccountRepository;
import com.portfolio.banking_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_savesWithUserId() {
        UUID userId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(
            "ACC-001", "Alex", new BigDecimal("500.00"), "USD"
        );

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.createAccount(request, userId);

        assertEquals("ACC-001", response.accountNumber());
        assertEquals(userId, response.userId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAllAccountsByUserId_returnsMappedAccounts() {
        UUID userId = UUID.randomUUID();
        Account account = new Account(
            "ACC-001", "Alex", userId, new BigDecimal("500.00"), "USD"
        );
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of(account));
        
        List<AccountResponse> response = accountService.getAllAccountsByUserId(userId);
        
        assertEquals(1, response.size());
        assertEquals("ACC-001", response.get(0).accountNumber());
        assertEquals(userId, response.get(0).userId());
    }

    @Test
    void getAllAccountsByUserId_returnsEmptyList() {
        UUID userId = UUID.randomUUID();
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of());
        
        assertTrue(accountService.getAllAccountsByUserId(userId).isEmpty());
    }
}
