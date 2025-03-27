package com.BankingService.AccountService.dto;

public class CustomerResponse {
    private final Long id;
    private final String email;

    public CustomerResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getEmail() { return email; }
}