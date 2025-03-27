package com.BankingSystem.NotificationService.kafka;

import com.BankingSystem.NotificationService.entity.NotifiedCustomer;
import com.BankingSystem.NotificationService.repository.NotifiedCustomerRepository;
import com.BankingSystem.NotificationService.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NotificationConsumer {

    private final EmailService emailService;
    private final NotifiedCustomerRepository notifiedCustomerRepository;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(EmailService emailService,
                                NotifiedCustomerRepository notifiedCustomerRepository,
                                ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.notifiedCustomerRepository = notifiedCustomerRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "transaction_notifications", groupId = "notification-service-group")
    public void processTransactionNotification(String message) {
        try {
            JsonNode messageJson = objectMapper.readTree(message);
            String senderEmail = messageJson.has("senderEmail") ? messageJson.get("senderEmail").asText() : null;
            String receiverEmail = messageJson.has("receiverEmail") ? messageJson.get("receiverEmail").asText() : null;
            BigDecimal amount = new BigDecimal(messageJson.get("amount").asText());

            if (senderEmail == null || receiverEmail == null) {
                return;
            }

            String subject = "Transaction Alert";
            String senderBody = "Dear Customer, you have sent ₹" + amount + ".";
            String receiverBody = "Dear Customer, you have received ₹" + amount + ".";

            emailService.sendEmail(senderEmail, subject, senderBody);
            emailService.sendEmail(receiverEmail, subject, receiverBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "low_balance_alerts", groupId = "notification-service-group")
    public void processLowBalanceAlert(String message) {
        try {
            JsonNode messageJson = objectMapper.readTree(message);
            Long customerId = messageJson.has("customerId") ? messageJson.get("customerId").asLong() : null;
            BigDecimal balance = messageJson.has("balance") ? new BigDecimal(messageJson.get("balance").asText()) : null;
            String email = messageJson.has("email") ? messageJson.get("email").asText() : null;

            if (customerId == null || balance == null || email == null) {
                return;
            }

            if (notifiedCustomerRepository.findByCustomerId(customerId).isPresent()) {
                return;
            }

            String subject = "Low Balance Alert";
            String body = "Dear Customer, your account balance is low: ₹" + balance + ". Please deposit funds.";

            emailService.sendEmail(email, subject, body);

            NotifiedCustomer notifiedCustomer = new NotifiedCustomer();
            notifiedCustomer.setCustomerId(customerId);
            notifiedCustomerRepository.save(notifiedCustomer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}