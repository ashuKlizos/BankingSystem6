package com.BankingService.AccountService.kafkaListner;

import com.BankingService.AccountService.dto.TransactionResponseDTO;
import com.BankingService.AccountService.model.Account;
import com.BankingService.AccountService.repository.AccountRepository;
import com.BankingService.AccountService.service.TransactionCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionEventConsumer {

    private final AccountRepository accountRepository;
    private final TransactionStatusProducer transactionStatusProducer;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;
    private final TransactionCacheService transactionCacheService;

    public TransactionEventConsumer(AccountRepository accountRepository,
                                    TransactionStatusProducer transactionStatusProducer,
                                    NotificationProducer notificationProducer,
                                    ObjectMapper objectMapper,
                                    TransactionCacheService transactionCacheService) {
        this.accountRepository = accountRepository;
        this.transactionStatusProducer = transactionStatusProducer;
        this.notificationProducer = notificationProducer;
        this.objectMapper = objectMapper;
        this.transactionCacheService = transactionCacheService;
    }

    @KafkaListener(topics = "transaction_requests", groupId = "account-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void processTransactionRequest(String transactionRequest, Acknowledgment acknowledgment) {
        try {
            TransactionResponseDTO transaction = objectMapper.readValue(transactionRequest, TransactionResponseDTO.class);

            if (transactionCacheService.isTransactionProcessed(transaction.getTransactionId())) {
                acknowledgment.acknowledge();
                return;
            }

            Optional<Account> senderOpt = accountRepository.findById(transaction.getSenderAccountId());
            Optional<Account> receiverOpt = accountRepository.findById(transaction.getReceiverAccountId());

            if (!senderOpt.isPresent() || !receiverOpt.isPresent()) {
                transactionStatusProducer.sendTransactionStatus(transaction.getTransactionId(), "FAILED", transaction.getSenderAccountId(), transaction.getReceiverAccountId(), transaction.getAmount());
                acknowledgment.acknowledge();
                return;
            }

            Account sender = senderOpt.get();
            Account receiver = receiverOpt.get();

            if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0 || sender.getBalance().compareTo(transaction.getAmount()) < 0) {
                transactionStatusProducer.sendTransactionStatus(transaction.getTransactionId(), "FAILED", transaction.getSenderAccountId(), transaction.getReceiverAccountId(), transaction.getAmount());
                acknowledgment.acknowledge();
                return;
            }

            sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));
            receiver.setBalance(receiver.getBalance().add(transaction.getAmount()));

            accountRepository.save(sender);
            accountRepository.save(receiver);

            transactionStatusProducer.sendTransactionStatus(
                    transaction.getTransactionId(),
                    "SUCCESS",
                    transaction.getSenderAccountId(),
                    transaction.getReceiverAccountId(),
                    transaction.getAmount()
            );

            notificationProducer.sendTransactionNotification(transaction.getSenderAccountId(), transaction.getReceiverAccountId(), transaction.getAmount());

            if (sender.getBalance().compareTo(new BigDecimal(500)) < 0) {
                notificationProducer.sendLowBalanceAlert(sender.getCustomerId(), sender.getBalance());
            } else {
                notificationProducer.resetLowBalanceNotification(sender.getCustomerId(), sender.getBalance());
            }

            transactionCacheService.markTransactionProcessed(transaction.getTransactionId());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}