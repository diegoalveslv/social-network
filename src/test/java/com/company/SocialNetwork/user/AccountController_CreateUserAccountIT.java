package com.company.SocialNetwork.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.company.SocialNetwork.user.AccountController.CREATE_USER_ACCOUNT_PATH;
import static com.company.SocialNetwork.utils.JsonUtils.asJsonString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountController_CreateUserAccountIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCreateUserAccountRequest")
    public void givenInvalidFieldInRequest_shouldReturnUnprocessableEntity(CreateUserAccountValidationRequest request) throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .content(asJsonString(request.getRequestDTO()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItems(request.getExpectedMessages())));
    }

    @ParameterizedTest
    @MethodSource("provideValidUserAccountRequest")
    public void givenValidRequest_shouldReturnCreated(CreateUserAccountValidationRequest request) throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request.getRequestDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist());
    }

    static Stream<CreateUserAccountValidationRequest> provideInvalidCreateUserAccountRequest() {
        //username
        var nullUsername = CreateUserAccountRequestModel.builder().username(null).build();
        var smallUsername = CreateUserAccountRequestModel.builder().username(randomAlphanumeric(2)).build();
        var bigUsername = CreateUserAccountRequestModel.builder().username(randomAlphanumeric(41)).build();
        var invalidFormatUsername1 = CreateUserAccountRequestModel.builder().username("@@@").build();
        var invalidFormatUsername2 = CreateUserAccountRequestModel.builder().username("a@a").build();
        var invalidFormatUsername3 = CreateUserAccountRequestModel.builder().username("any.").build();
        var invalidFormatUsername4 = CreateUserAccountRequestModel.builder().username("any:").build();
        //email
        var nullEmail = CreateUserAccountRequestModel.builder().email(null).build();
//        var validSmallEmail = CreateUserAccountRequestModel.builder().email("a@b.c").build(); //TODO smallest valid
        var bigEmail = CreateUserAccountRequestModel.builder().email(randomAlphabetic(64) + "@" + randomAlphabetic(187) + ".com").build();
        var invalidEmail1 = CreateUserAccountRequestModel.builder().email("email").build();
        var invalidEmail2 = CreateUserAccountRequestModel.builder().email("emailll@").build();
        var invalidEmail3 = CreateUserAccountRequestModel.builder().email("@email").build();
        //password
        var nullPassword = CreateUserAccountRequestModel.builder().password(null).build();
        var blankPassword = CreateUserAccountRequestModel.builder().password("").build();
        var smallPassword = CreateUserAccountRequestModel.builder().password(randomAlphanumeric(7)).build();
        var bigPassword = CreateUserAccountRequestModel.builder().password(randomAlphanumeric(61)).build();
        var weakPassword1 = CreateUserAccountRequestModel.builder().password("weakpass").build();
        //profileName
        var nullProfileName = CreateUserAccountRequestModel.builder().profileName(null).build();
        var blankProfileName = CreateUserAccountRequestModel.builder().profileName("").build();
        var smallProfileName = CreateUserAccountRequestModel.builder().profileName(randomAlphabetic(1)).build();
        var bigProfileName = CreateUserAccountRequestModel.builder().profileName(randomAlphabetic(41)).build();
        var xssAttackProfileName = CreateUserAccountRequestModel.builder().profileName("<script>alert('XSS')</script>").build();

        return Stream.of(
                //username
                CreateUserAccountValidationRequest.of(nullUsername, "username: must not be blank")
                , CreateUserAccountValidationRequest.of(smallUsername, "username: size must be between 3 and 40")
                , CreateUserAccountValidationRequest.of(bigUsername, "username: size must be between 3 and 40")
                , CreateUserAccountValidationRequest.of(invalidFormatUsername1, "username: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
                , CreateUserAccountValidationRequest.of(invalidFormatUsername2, "username: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
                , CreateUserAccountValidationRequest.of(invalidFormatUsername3, "username: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
                , CreateUserAccountValidationRequest.of(invalidFormatUsername4, "username: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
                //email
                , CreateUserAccountValidationRequest.of(nullEmail, "email: must not be blank")
                , CreateUserAccountValidationRequest.of(bigEmail, "email: size must be between 5 and 255")
                , CreateUserAccountValidationRequest.of(invalidEmail1, "email: must be a well-formed email address")
                , CreateUserAccountValidationRequest.of(invalidEmail2, "email: must be a well-formed email address")
                , CreateUserAccountValidationRequest.of(invalidEmail3, "email: must be a well-formed email address")
                //password
                , CreateUserAccountValidationRequest.of(nullPassword, "password: must not be blank")
                , CreateUserAccountValidationRequest.of(blankPassword, "password: must not be blank")
                , CreateUserAccountValidationRequest.of(smallPassword, "password: size must be between 8 and 60")
                , CreateUserAccountValidationRequest.of(bigPassword, "password: size must be between 8 and 60")
                , CreateUserAccountValidationRequest.of(weakPassword1, "password: does not meet security requirements"
                        , "password: should have at least one lowercase letter"
                        , "password: should have at least one uppercase letter"
                        , "password: should have at least one special character")
                //profileName
                , CreateUserAccountValidationRequest.of(nullProfileName, "profileName: must not be blank")
                , CreateUserAccountValidationRequest.of(blankProfileName, "profileName: must not be blank")
                , CreateUserAccountValidationRequest.of(smallProfileName, "profileName: size must be between 2 and 40")
                , CreateUserAccountValidationRequest.of(bigProfileName, "profileName: size must be between 2 and 40")
                , CreateUserAccountValidationRequest.of(xssAttackProfileName, "profileName: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
        );
    }

    static Stream<CreateUserAccountValidationRequest> provideValidUserAccountRequest() {
        final String strongPassword1 = "Strong@password1";
        final String strongPassword2 = "St@word1";
        final String validEmail1 = "email@email";
        final String validEmail2 = "a@b.c";
        final String validUsername2 = "User_name-1";
        final String validProfileName3 = "Valid-Profile_Name da Silva";

        var validUsernameModel1 = CreateUserAccountRequestModel.builder().username(validUsername2).password(strongPassword1).email(validEmail1).profileName(validProfileName3).build();
        var validUsernameModel2 = CreateUserAccountRequestModel.builder().username(validUsername2).password(strongPassword2).email(validEmail2).profileName(validProfileName3).build();
        return Stream.of(
                CreateUserAccountValidationRequest.of(validUsernameModel1)
                , CreateUserAccountValidationRequest.of(validUsernameModel2)
        );
    }
}
