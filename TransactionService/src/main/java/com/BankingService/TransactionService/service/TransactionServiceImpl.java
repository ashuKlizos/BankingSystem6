package com.BankingService.TransactionService.service;

import com.BankingService.TransactionService.client.AccountServiceClient;
import com.BankingService.TransactionService.dto.TransactionRequestDTO;
import com.BankingService.TransactionService.dto.TransactionResponseDTO;
import com.BankingService.TransactionService.entity.Transaction;
import com.BankingService.TransactionService.entity.TransactionStatus;
import com.BankingService.TransactionService.entity.TransactionType;
import com.BankingService.TransactionService.kafkaTemplate.TransactionEventProducer;
import com.BankingService.TransactionService.repository.TransactionRepository;
import com.BankingService.TransactionService.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;
    private final AccountServiceClient accountServiceClient;
    private final TransactionEventProducer transactionEventProducer;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransactionResponseDTO performTransaction(String token, TransactionRequestDTO transactionRequest) {

        Long customerId = jwtUtil.extractCustomerId(token);

        Long senderCustomerId = accountServiceClient.getCustomerIdByAccountId(transactionRequest.getSenderAccountId(), token);

        if (!customerId.equals(senderCustomerId)) {
            throw new RuntimeException("Unauthorized transaction attempt");
        }

        TransactionType transactionType = determineTransactionType(transactionRequest.getSenderAccountId(), transactionRequest.getReceiverAccountId());

        Transaction transaction = Transaction.builder()
                .senderAccountId(transactionRequest.getSenderAccountId())
                .receiverAccountId(transactionRequest.getReceiverAccountId())
                .amount(transactionRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .transactionType(transactionType)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        eventPublisher.publishEvent(new TransactionEvent(savedTransaction));

        return mapToDTO(savedTransaction);
    }

    @TransactionalEventListener
    public void handleTransactionEvent(TransactionEvent event) {
        transactionEventProducer.sendTransactionRequest(event.getTransaction());
    }

    @Override
    public TransactionResponseDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return mapToDTO(transaction);
    }

    private TransactionType determineTransactionType(Long senderAccountId, Long receiverAccountId) {
        return senderAccountId.equals(receiverAccountId) ? TransactionType.INTERNAL : TransactionType.EXTERNAL;
    }

    private TransactionResponseDTO mapToDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getTransactionId(),
                transaction.getSenderAccountId(),
                transaction.getReceiverAccountId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getTransactionDate()
        );
    }
}