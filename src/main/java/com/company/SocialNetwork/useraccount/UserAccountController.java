package com.company.SocialNetwork.useraccount;

import com.company.SocialNetwork.utils.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Account")
public class UserAccountController {

    public static final String CREATE_USER_ACCOUNT_PATH = "/users";

    private final UserAccountService userAccountService;

    @Operation(summary = "Create user account", operationId = "createUserAccount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "CREATED")
    })
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
