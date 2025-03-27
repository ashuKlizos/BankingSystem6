package com.BankingService.AccountService.kafkaListner;

import com.BankingService.AccountService.dto.CustomerResponse;
import com.BankingService.AccountService.model.Account;
import com.BankingService.AccountService.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final AccountRepository accountRepository;

    private final String customerServiceUrl = "http://localhost:8081/api/users/email/";

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, RestTemplate restTemplate, StringRedisTemplate redisTemplate, AccountRepository accountRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.accountRepository = accountRepository;
    }

    public void sendTransactionNotification(Long senderAccountId, Long receiverAccountId, BigDecimal amount) {
        try {
            Long senderCustomerId = getCustomerIdByAccountId(senderAccountId);
            Long receiverCustomerId = getCustomerIdByAccountId(receiverAccountId);

            if (senderCustomerId == null || receiverCustomerId == null) {
                return;
            }

            String senderEmail = fetchCustomerEmail(senderCustomerId);
            String receiverEmail = fetchCustomerEmail(receiverCustomerId);

            if (senderEmail == null || receiverEmail == null) {
                return;
            }

            Map<String, Object> message = new HashMap<>();
            message.put("type", "TRANSACTION");
            message.put("senderCustomerId", senderCustomerId);
            message.put("receiverCustomerId", receiverCustomerId);
            message.put("senderEmail", senderEmail);
            message.put("receiverEmail", receiverEmail);
            message.put("amount", amount);

            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("transaction_notifications", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendLowBalanceAlert(Long customerId, BigDecimal balance) {
        try {
            String cacheKey = "low_balance_notified:" + customerId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
                return;
            }

            String email = fetchCustomerEmail(customerId);
            if (email == null) {
                return;
            }

            Map<String, Object> message = new HashMap<>();
            message.put("type", "LOW_BALANCE_ALERT");
            message.put("customerId", customerId);
            message.put("balance", balance);
            message.put("email", email);

            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("low_balance_alerts", jsonMessage);

            redisTemplate.opsForValue().set(cacheKey, "true", Duration.ofHours(24));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetLowBalanceNotification(Long customerId, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(500)) >= 0) {
            String cacheKey = "low_balance_notified:" + customerId;
            redisTemplate.delete(cacheKey);
        }
    }

    private String fetchCustomerEmail(Long customerId) {
        try {
            String url = customerServiceUrl + customerId;
            ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(url, CustomerResponse.class);
            return response.getBody() != null ? response.getBody().getEmail() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCustomerIdByAccountId(Long accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        return account.map(Account::getCustomerId).orElse(null);
    }
}