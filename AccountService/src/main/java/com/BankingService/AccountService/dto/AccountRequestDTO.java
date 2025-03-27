package com.BankingService.AccountService.dto;

import com.BankingService.AccountService.model.AccountType;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountRequestDTO {

    @NotNull
    private AccountType accountType;

}