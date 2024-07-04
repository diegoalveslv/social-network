package com.company.SocialNetwork.useraccount;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UserAccountController {

    public static final String CREATE_USER_ACCOUNT_PATH = "/users";

    private final UserAccountService userAccountService;

    @PostMapping(CREATE_USER_ACCOUNT_PATH)
    public ResponseEntity<Void> createUserAccount(@RequestBody CreateUserAccountRequestDTO createUserAccountRequest) {

        String slug = userAccountService.createUserAccount(createUserAccountRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{slug}")
                .buildAndExpand(slug)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
