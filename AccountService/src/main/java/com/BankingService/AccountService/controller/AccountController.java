package com.BankingService.AccountService.controller;

import com.BankingService.AccountService.dto.AccountRequestDTO;
import com.BankingService.AccountService.dto.AccountResponseDTO;
import com.BankingService.AccountService.service.AccountService;
import com.BankingService.AccountService.service.JwtService;
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


    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountRequestDTO accountRequest) {
        return ResponseEntity.ok(accountService.createAccount(accountRequest, getCustomerId()));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @GetMapping("/balance/{customerId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAccountBalances(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountBalances(customerId));
    }

    @GetMapping("/status/{customerId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAccountStatuses(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountStatuses(customerId));
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

    private String getAuthToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getCredentials().toString();
    }

    private Long getCustomerId() {
        String token = getAuthToken();
        String customerId = jwtService.getCustomerId(token);
        return Long.valueOf(customerId);
    }
}