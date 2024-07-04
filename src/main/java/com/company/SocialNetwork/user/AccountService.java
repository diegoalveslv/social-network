package com.company.SocialNetwork.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class AccountService {

    public void createUserAccount(@Valid CreateUserAccountRequestDTO createUserAccountRequest) {
    }
}
