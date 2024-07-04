package com.company.SocialNetwork.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    public static final String CREATE_USER_ACCOUNT_PATH = "/users";

    @PostMapping(CREATE_USER_ACCOUNT_PATH)
    public ResponseEntity<Void> createUserAccount(@Validated @RequestBody CreateUserAccountRequestDTO createUserAccountRequest) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
