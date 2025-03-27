package com.BankingService.TransactionService.kafkaTemplate;

import com.BankingService.TransactionService.entity.Transaction;
import com.BankingService.TransactionService.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;

    private final Set<Long> sentTransactions = ConcurrentHashMap.newKeySet(); // ✅ Prevent duplicate sends in-memory

    public TransactionEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                    ObjectMapper objectMapper,
                                    TransactionRepository transactionRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.transactionRepository = transactionRepository;
    }

    public void sendTransactionRequest(Transaction transaction) {
        try {
            if (sentTransactions.contains(transaction.getTransactionId())) {
                System.out.println("⚠️ Transaction " + transaction.getTransactionId() + " already sent in this instance, skipping...");
                return;
            }

            String transactionJson = objectMapper.writeValueAsString(transaction);
            kafkaTemplate.send("transaction_requests", transactionJson).get(); // Ensure Kafka gets it

            sentTransactions.add(transaction.getTransactionId()); // ✅ Mark as sent in this instance
            System.out.println("✅ Sent Transaction Request: " + transactionJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}