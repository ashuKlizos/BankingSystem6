package com.BankingService.AccountService.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TransactionCacheService {
    private final StringRedisTemplate redisTemplate;
    private static final String TRANSACTION_PREFIX = "transaction:";

    public TransactionCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isTransactionProcessed(Long transactionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TRANSACTION_PREFIX + transactionId));
    }

    public void markTransactionProcessed(Long transactionId) {
        redisTemplate.opsForValue().set(TRANSACTION_PREFIX + transactionId, "PROCESSED", Duration.ofMinutes(10));
    }
}