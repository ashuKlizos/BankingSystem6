package com.BankingSystem.NotificationService.repository;

import com.BankingSystem.NotificationService.entity.NotifiedCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotifiedCustomerRepository extends JpaRepository<NotifiedCustomer, Long> {
    Optional<NotifiedCustomer> findByCustomerId(Long customerId);
}
