package com.BankingSystem.CutomerrService.dto;

public class CustomerResponse {
    private final Long id;
    private final String email;

    public CustomerResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
}