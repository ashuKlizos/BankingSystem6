package com.BankingService.TransactionService.kafkaTemplate;

import com.BankingService.TransactionService.entity.Transaction;
import com.BankingService.TransactionService.entity.TransactionStatus;
import com.BankingService.TransactionService.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class TransactionConsumer {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    public TransactionConsumer(TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "transaction_responses", groupId = "transaction-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void processTransactionResponse(String transactionResponse, Acknowledgment acknowledgment) {
        try {
            if (!isValidJson(transactionResponse)) {
                acknowledgment.acknowledge();
                return;
            }

            JsonNode jsonNode = objectMapper.readTree(transactionResponse);

            if (!jsonNode.has("transactionId") || !jsonNode.has("status")) {
                acknowledgment.acknowledge();
                return;
            }

            Long transactionId = jsonNode.get("transactionId").asLong();
            String status = jsonNode.get("status").asText();

            Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

            transaction.setStatus(TransactionStatus.valueOf(status));
            transactionRepository.save(transaction);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidJson(String input) {
        try {
            objectMapper.readTree(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}