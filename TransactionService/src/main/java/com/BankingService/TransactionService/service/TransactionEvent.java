package com.BankingService.TransactionService.service;

import com.BankingService.TransactionService.entity.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionEvent extends ApplicationEvent {
    private final Transaction transaction;

    public TransactionEvent(Transaction transaction) {
        super(transaction);
        this.transaction = transaction;
    }
}