package com.BankingService.AccountService.controller;

import com.BankingService.AccountService.dto.AccountRequestDTO;
import com.BankingService.AccountService.dto.AccountResponseDTO;
import com.BankingService.AccountService.service.AccountService;
import com.BankingService.AccountService.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtService jwtService;

    private String getAuthToken() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    private Long getCustomerId() {
        return Long.valueOf(jwtService.getCustomerId(getAuthToken()));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAccount(@RequestBody AccountRequestDTO accountRequest, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
                ? authorizationHeader.substring(7)
                : null;

        Long customerId = Long.parseLong(jwtService.getCustomerId(token));
        AccountResponseDTO responseDTO = accountService.createAccount(accountRequest, customerId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @GetMapping("/balance/{customerId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAccountBalances(@PathVariable Long customerId) {
        List<String> balances = accountService.getAccountsByCustomerId(customerId).stream()
                .map(account -> account.getAccountNumber() + " - " + accountService.getAccountBalance(customerId))
                .toList();
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/status/{customerId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAccountStatuses(@PathVariable Long customerId) {
        List<String> statuses = accountService.getAccountsByCustomerId(customerId).stream()
                .map(account -> account.getAccountNumber() + " - " + accountService.getAccountStatus(customerId))
                .toList();
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByLoggedInCustomer() {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(getCustomerId()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{accountId}/customer-id")
    public Long getCustomerIdByAccountId(@PathVariable Long accountId) {
        return accountService.getCustomerIdByAccountId(accountId);
    }
}