package com.BankingService.TransactionService.dto;

import com.BankingService.TransactionService.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private Long senderAccountId;
    private Long receiverAccountId;
    private BigDecimal amount;
}