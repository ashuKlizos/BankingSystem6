package com.BankingService.TransactionService.service;

import com.BankingService.TransactionService.dto.TransactionRequestDTO;
import com.BankingService.TransactionService.dto.TransactionResponseDTO;

public interface TransactionService {
    TransactionResponseDTO performTransaction(String token, TransactionRequestDTO transactionRequest); // Added token
    TransactionResponseDTO getTransactionById(Long transactionId);
}

