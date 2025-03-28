package com.BankingService.AccountService.service;

import com.BankingService.AccountService.client.CustomerClient;
import com.BankingService.AccountService.dto.AccountRequestDTO;
import com.BankingService.AccountService.dto.AccountResponseDTO;
import com.BankingService.AccountService.model.Account;
import com.BankingService.AccountService.model.AccountStatus;
import com.BankingService.AccountService.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient;
    private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();

    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO request, Long customerId) {
        boolean exists = customerClient.checkCustomerExists(customerId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer does not exist.");
        }

        Account account = new Account();
        account.setAccountNumber("ACCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.valueOf(5000));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomerId(customerId);

        Account savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    public List<AccountResponseDTO> getAccountsByCustomerId(Long id) {
        List<Account> accounts = accountRepository.findByCustomerId(id);
        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No accounts found for this customer");
        }
        return accounts.stream().map(this::convertToDTO).toList();
    }

    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<String> getAccountBalances(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No accounts found for this customer.");
        }

        return accounts.stream()
                .map(account -> account.getAccountNumber() + " - Balance: " + account.getBalance().toString())
                .toList();
    }

    public List<String> getAccountStatuses(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No accounts found for this customer.");
        }

        return accounts.stream()
                .map(account -> account.getAccountNumber() + " - Status: " + account.getStatus().toString())
                .toList();
    }

    public Long getCustomerIdByAccountId(Long accountId) {
        Account account = accountRepository.findByAccountId(accountId);
        if (account == null) {
            throw new RuntimeException("Account not found4");
        }
        return account.getCustomerId();
    }

    private AccountResponseDTO convertToDTO(Account account) {
        return new AccountResponseDTO(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCustomerId()
        );
    }
}