package com.company.SocialNetwork.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.stream.Stream;

import static com.company.SocialNetwork.TestUtils.extractMessagesFromBody;
import static com.company.SocialNetwork.user.AccountController.CREATE_USER_ACCOUNT_PATH;
import static com.company.SocialNetwork.utils.JsonUtils.asJsonString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
    @MethodSource("provideInvalidUserAccountRequest")
    public void givenInvalidRequest_shouldReturnBadRequest(InvalidCreateUserAccountRequestDTO request) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .content(asJsonString(request.getRequestDTO()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<String> messages = extractMessagesFromBody(contentAsString);
        assertThat(messages).containsAnyOf(request.getExpectedMessage());
    }

    @Test
    public void givenWeakPassword_shouldReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CreateUserAccountRequestModel.builder()
                                .username("username")
                                .email("email@email")
                                .password("weakpass")
                                .profileName("profileName")
                                .build())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(contains("password: does not meet security requirements"
                        , "Should have at least one lowercase letter"
                        , "Should have at least one uppercase letter"
                        , "Should have at least one special character"
                )));
    }

    @Test
    public void givenStrongPassword_shouldReturnCreated() throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CreateUserAccountRequestModel.builder()
                                .username("username")
                                .email("email@email")
                                .password("Strong@password1")
                                .profileName("profileName").build())))
                .andExpect(status().isCreated());
    }

    static Stream<InvalidCreateUserAccountRequestDTO> provideInvalidUserAccountRequest() {
        var nullUsername = CreateUserAccountRequestModel.builder().username(null).build();
        var smallUsername = CreateUserAccountRequestModel.builder().username(randomAlphanumeric(2)).build();
        var bigUsername = CreateUserAccountRequestModel.builder().username(randomAlphanumeric(41)).build();
        var nullEmail = CreateUserAccountRequestModel.builder().email(null).build();
//        var validSmallEmail = CreateUserAccountRequestModel.builder().email("a@b.c").build(); //TODO smallest valid
        var bigEmail = CreateUserAccountRequestModel.builder().email(randomAlphabetic(64) + "@" + randomAlphabetic(187) + ".com").build();
        var invalidEmail1 = CreateUserAccountRequestModel.builder().email("email").build();
        var invalidEmail2 = CreateUserAccountRequestModel.builder().email("emailll@").build();
        var invalidEmail3 = CreateUserAccountRequestModel.builder().email("@email").build();
        var nullPassword = CreateUserAccountRequestModel.builder().password(null).build();
        var blankPassword = CreateUserAccountRequestModel.builder().password("").build();
        var smallPassword = CreateUserAccountRequestModel.builder().password(randomAlphanumeric(7)).build();
        var bigPassword = CreateUserAccountRequestModel.builder().password(randomAlphanumeric(61)).build();
        var nullProfileName = CreateUserAccountRequestModel.builder().profileName(null).build();
        var blankProfileName = CreateUserAccountRequestModel.builder().profileName("").build();
        var smallProfileName = CreateUserAccountRequestModel.builder().profileName(randomAlphabetic(1)).build();
        var bigProfileName = CreateUserAccountRequestModel.builder().profileName(randomAlphabetic(41)).build();
        return Stream.of(InvalidCreateUserAccountRequestDTO.of(nullUsername, "username: must not be blank")
                , InvalidCreateUserAccountRequestDTO.of(smallUsername, "username: size must be between 3 and 40")
                , InvalidCreateUserAccountRequestDTO.of(bigUsername, "username: size must be between 3 and 40")
                , InvalidCreateUserAccountRequestDTO.of(nullEmail, "email: must not be blank")
                , InvalidCreateUserAccountRequestDTO.of(bigEmail, "email: size must be between 5 and 255")
                , InvalidCreateUserAccountRequestDTO.of(invalidEmail1, "email: must be a well-formed email address")
                , InvalidCreateUserAccountRequestDTO.of(invalidEmail2, "email: must be a well-formed email address")
                , InvalidCreateUserAccountRequestDTO.of(invalidEmail3, "email: must be a well-formed email address")
                , InvalidCreateUserAccountRequestDTO.of(nullPassword, "password: must not be blank")
                , InvalidCreateUserAccountRequestDTO.of(blankPassword, "password: must not be blank")
                , InvalidCreateUserAccountRequestDTO.of(smallPassword, "password: size must be between 8 and 60")
                , InvalidCreateUserAccountRequestDTO.of(bigPassword, "password: size must be between 8 and 60")
                , InvalidCreateUserAccountRequestDTO.of(nullProfileName, "profileName: must not be blank")
                , InvalidCreateUserAccountRequestDTO.of(blankProfileName, "profileName: must not be blank")
                , InvalidCreateUserAccountRequestDTO.of(smallProfileName, "profileName: size must be between 2 and 40")
                , InvalidCreateUserAccountRequestDTO.of(bigProfileName, "profileName: size must be between 2 and 40")
        );
    }
}
