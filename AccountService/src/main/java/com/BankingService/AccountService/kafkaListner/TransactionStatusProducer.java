package com.BankingService.AccountService.kafkaListner;

import com.BankingService.AccountService.dto.TransactionResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionStatusProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TransactionStatusProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendTransactionStatus(Long transactionId, String status, Long senderAccountId, Long receiverAccountId, BigDecimal amount) {
        try {
            if (transactionId == null) {
                return;
            }

            TransactionResponseDTO response = new TransactionResponseDTO(transactionId, status, senderAccountId, receiverAccountId, amount);
            String jsonResponse = objectMapper.writeValueAsString(response);
            kafkaTemplate.send("transaction_responses", jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}