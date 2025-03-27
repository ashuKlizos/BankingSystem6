package com.BankingService.TransactionService.client;

import com.BankingService.TransactionService.config.FeignClientConfig;
import com.BankingService.TransactionService.dto.TransactionRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ACCOUNTSERVICE", url = "http://localhost:8082")
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{id}/customer-id")
    Long getCustomerIdByAccountId(@PathVariable("id") Long id,
                                  @RequestHeader("Authorization") String token);
}