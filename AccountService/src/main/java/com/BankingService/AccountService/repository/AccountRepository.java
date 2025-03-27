package com.BankingService.AccountService.repository;


import com.BankingService.AccountService.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerId(Long customerId);
    Account findByAccountId(Long accountId);
}