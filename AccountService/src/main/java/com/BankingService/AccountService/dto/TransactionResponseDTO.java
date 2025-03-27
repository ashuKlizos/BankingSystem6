package com.BankingService.AccountService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private Long transactionId;
    private String status;
    private Long senderAccountId;
    private Long receiverAccountId;
    private BigDecimal amount;
}