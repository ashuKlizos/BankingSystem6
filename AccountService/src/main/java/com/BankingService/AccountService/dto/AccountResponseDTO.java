package com.BankingService.AccountService.dto;

import com.BankingService.AccountService.model.AccountStatus;
import com.BankingService.AccountService.model.AccountType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponseDTO {
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private Long customerId;
}