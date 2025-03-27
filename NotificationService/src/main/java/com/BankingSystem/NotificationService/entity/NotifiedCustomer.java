package com.BankingSystem.NotificationService.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="notifications")
public class NotifiedCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
}