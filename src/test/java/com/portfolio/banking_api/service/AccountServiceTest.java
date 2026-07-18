package com.portfolio.banking_api.service;

import com.portfolio.banking_api.domain.Account;
import com.portfolio.banking_api.domain.Transaction;
import com.portfolio.banking_api.domain.TransactionType;
import com.portfolio.banking_api.dto.AccountResponse;
import com.portfolio.banking_api.dto.CreateAccountRequest;
import com.portfolio.banking_api.dto.MoneyRequest;
import com.portfolio.banking_api.dto.TransactionResponse;
import com.portfolio.banking_api.dto.TransferRequest;
import com.portfolio.banking_api.repository.AccountRepository;
import com.portfolio.banking_api.repository.TransactionRepository;
import com.portfolio.banking_api.exception.AccountNotFoundException;
import com.portfolio.banking_api.exception.BadRequestException;
import com.portfolio.banking_api.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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

    @Test
    void deposit_creditsAndSavesTransaction() {
        UUID userId = UUID.randomUUID();
        
        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );

        when(accountRepository.findByIdAndUserId(account.getId(), userId))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = accountService.deposit(
            account.getId(), userId, new MoneyRequest(new BigDecimal("100.00")));

        assertEquals(new BigDecimal("1100.00"), account.getBalance());
        assertEquals(TransactionType.DEPOSIT, response.type());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(account.getId(), response.accountId());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_throwsWhenAccountNotFound() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findByIdAndUserId(accountId, userId))
            .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
            accountService.deposit(accountId, userId, new MoneyRequest(new BigDecimal("100.00"))));
    }

    @Test
    void withdraw_debitsAndSavesTransaction() {
        UUID userId = UUID.randomUUID();

        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );

        when(accountRepository.findByIdAndUserId(account.getId(), userId))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = accountService.withdraw(
            account.getId(), userId, new MoneyRequest(new BigDecimal("100.00")));

        assertEquals(new BigDecimal("900.00"), account.getBalance());
        assertEquals(TransactionType.WITHDRAWAL, response.type());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(account.getId(), response.accountId());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_throwsWhenAccountNotFound() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserId(accountId, userId))
            .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
            accountService.withdraw(accountId, userId, new MoneyRequest(new BigDecimal("100.00"))));
    }

    @Test
    void withdraw_throwsWhenInsufficientFunds() {
        UUID userId = UUID.randomUUID();

        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );

        when(accountRepository.findByIdAndUserId(account.getId(), userId))
            .thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () ->
            accountService.withdraw(account.getId(), userId, new MoneyRequest(new BigDecimal("1100.00"))));
    }

    @Test
    void transfer_movesFundsAndSavesBothLegs() {
        UUID sourceUserId = UUID.randomUUID();
        UUID destinationUserId = UUID.randomUUID();

        Account sourceAccount = new Account(
            "1234567890",
            "Alex Morgan",
            sourceUserId,
            new BigDecimal("1000.00"),
            "USD"
        );
        Account destinationAccount = new Account(
            "1234567891",
            "Benjamin Smith",
            destinationUserId,
            new BigDecimal("1000.00"),
            "USD"
        );

        when(accountRepository.findByIdAndUserId(sourceAccount.getId(), sourceUserId))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("1234567891"))
            .thenReturn(Optional.of(destinationAccount));
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = accountService.transfer(
            sourceAccount.getId(),
            sourceUserId,
            new TransferRequest(new BigDecimal("100.00"), "1234567891")
        );

        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("1100.00"), destinationAccount.getBalance());
        assertEquals(TransactionType.TRANSFER_OUT, response.type());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(sourceAccount.getId(), response.accountId());
        assertEquals(destinationAccount.getId(), response.counterpartyAccountId());
        assertNotNull(response.transferGroupId());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_throwsWhenSourceAccountNotFound() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserId(accountId, userId))
            .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
            accountService.transfer(accountId, userId, new TransferRequest(new BigDecimal("100.00"), "1234567891")));
    }

    @Test
    void transfer_throwsWhenDestinationAccountNotFound() {
        UUID userId = UUID.randomUUID();

        Account sourceAccount = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );

        when(accountRepository.findByIdAndUserId(sourceAccount.getId(), userId))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("1234567891"))
            .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
            accountService.transfer(sourceAccount.getId(), userId, new TransferRequest(new BigDecimal("100.00"), "1234567891")));
    }

    @Test
    void transfer_throwsWhenSameAccount() {
        UUID userId = UUID.randomUUID();

        Account account = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );

        when(accountRepository.findByIdAndUserId(account.getId(), userId))
            .thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber(account.getAccountNumber()))
            .thenReturn(Optional.of(account));

        assertThrows(BadRequestException.class, () ->
            accountService.transfer(account.getId(), userId, new TransferRequest(new BigDecimal("100.00"), account.getAccountNumber())));
    }

    @Test
    void transfer_throwsWhenCurrencyMismatch() {
        UUID userId = UUID.randomUUID();

        Account sourceAccount = new Account(
            "1234567890",
            "Alex Morgan",
            userId,
            new BigDecimal("1000.00"),
            "USD"
        );
        Account destinationAccount = new Account(
            "1234567891",
            "Benjamin Smith",
            userId,
            new BigDecimal("1000.00"),
            "EUR"
        );

        when(accountRepository.findByIdAndUserId(sourceAccount.getId(), userId))
            .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("1234567891"))
            .thenReturn(Optional.of(destinationAccount));

        assertThrows(BadRequestException.class, () ->
            accountService.transfer(sourceAccount.getId(), userId, new TransferRequest(new BigDecimal("100.00"), "1234567891")));
    }
}
