package com.BankingSystem.CutomerrService.dto;

import com.BankingSystem.CutomerrService.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String dob;
    private Role role;
}
