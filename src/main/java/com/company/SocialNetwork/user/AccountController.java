package com.company.SocialNetwork.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    public static final String CREATE_USER_ACCOUNT_PATH = "/users";

    private final AccountService accountService;

    @PostMapping(CREATE_USER_ACCOUNT_PATH)
    public ResponseEntity<Void> createUserAccount(@RequestBody CreateUserAccountRequestDTO createUserAccountRequest) {

        accountService.createUserAccount(createUserAccountRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
