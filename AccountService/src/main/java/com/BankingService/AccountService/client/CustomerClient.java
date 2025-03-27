package com.BankingService.AccountService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMERRSERVICE")
public interface CustomerClient {

    @GetMapping("/api/users/exists/{id}")
    boolean checkCustomerExists(@PathVariable("id") Long id);

}