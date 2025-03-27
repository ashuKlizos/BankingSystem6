package com.BankingService.TransactionService.controller;

import com.BankingService.TransactionService.dto.TransactionRequestDTO;
import com.BankingService.TransactionService.dto.TransactionResponseDTO;
import com.BankingService.TransactionService.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> performTransaction(
            @RequestHeader("Authorization") String token,
            @RequestBody TransactionRequestDTO transactionRequest) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        TransactionResponseDTO response = transactionService.performTransaction(jwtToken, transactionRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable Long id) {
        TransactionResponseDTO response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }
}