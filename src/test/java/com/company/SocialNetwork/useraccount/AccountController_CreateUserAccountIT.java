package com.company.SocialNetwork.useraccount;

import com.company.SocialNetwork.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.company.SocialNetwork.useraccount.UserAccountController.CREATE_USER_ACCOUNT_PATH;
import static com.company.SocialNetwork.utils.JsonUtils.asJsonString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class AccountController_CreateUserAccountIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void givenBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCreateUserAccountRequest")
    public void givenInvalidFieldInRequest_shouldReturnUnprocessableEntity(CreateUserAccountRequestModel request, String expectedMessage) throws Exception {
        mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .content(asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItems(expectedMessage)));
    }

    @ParameterizedTest
    @MethodSource("provideValidUserAccountRequest")
    @Sql(scripts = "classpath:db-scripts/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void givenValidRequest_shouldReturnCreated(CreateUserAccountRequestModel request) throws Exception {
        var result = mockMvc.perform(post(CREATE_USER_ACCOUNT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().exists("location"))
                .andExpect(header().string("location", matchesPattern("http://localhost/users/\\w{12}")))
                .andReturn();

        String location = result.getResponse().getHeader("location");
        assertThat(location).isNotNull();

        String slug = getSlugFromLocation(location);
        shouldExistInDatabase(slug);

        var userAccount = findUserAccountBySlug(slug);
        shouldHaveEncryptedPassword(request.getPassword(), userAccount.getPassword());
        shouldHaveSetCreatedDate(userAccount);
        shouldHaveTrimmedTextFields(userAccount, request);
    }

    //TODO test scenarios where duplication occurs

    private void shouldHaveTrimmedTextFields(UserAccount userAccount, CreateUserAccountRequestModel request) {
        assertThat(userAccount.getUsername()).isEqualTo(request.getUsername().trim());
        assertThat(userAccount.getProfileName()).isEqualTo(request.getProfileName().trim());
    }

    private void shouldHaveSetCreatedDate(UserAccount userAccount) {
        assertThat(userAccount.getCreatedAt()).isNotNull();
    }

    private void shouldHaveEncryptedPassword(String requestPassword, String savedPassword) {
        assertThat(savedPassword).isNotEqualTo(requestPassword);
    }

    private void shouldExistInDatabase(String slug) {
        try {
            var result = findUserAccountBySlug(slug);
            assertThat(result).isNotNull();
        } catch (NoResultException e) {
            fail("Slug " + slug + " not found in database");
        }
    }

    static Stream<Arguments> provideInvalidCreateUserAccountRequest() {
        //username
        var nullUsername = new CreateUserAccountRequestModel().setUsername(null);
        var blankUsername = new CreateUserAccountRequestModel().setUsername("");
        var smallUsername = new CreateUserAccountRequestModel().setUsername(randomAlphanumeric(2));
        var bigUsername = new CreateUserAccountRequestModel().setUsername(randomAlphanumeric(41));
        var invalidFormatUsername1 = new CreateUserAccountRequestModel().setUsername("@@@");
        var invalidFormatUsername2 = new CreateUserAccountRequestModel().setUsername("a@a");
        var invalidFormatUsername3 = new CreateUserAccountRequestModel().setUsername("any.");
        var invalidFormatUsername4 = new CreateUserAccountRequestModel().setUsername("any:");
        var invalidFormatUsername5 = new CreateUserAccountRequestModel().setUsername("user name");
        //email
        var nullEmail = new CreateUserAccountRequestModel().setEmail(null);
        var bigEmail = new CreateUserAccountRequestModel().setEmail(randomAlphabetic(64) + "@" + randomAlphabetic(187) + ".com");
        var invalidEmail1 = new CreateUserAccountRequestModel().setEmail("email");
        var invalidEmail2 = new CreateUserAccountRequestModel().setEmail("emailll@");
        var invalidEmail3 = new CreateUserAccountRequestModel().setEmail("@email");
        //password
        var nullPassword = new CreateUserAccountRequestModel().setPassword(null);
        var blankPassword = new CreateUserAccountRequestModel().setPassword("");
        var smallPassword = new CreateUserAccountRequestModel().setPassword(randomAlphanumeric(7));
        var bigPassword = new CreateUserAccountRequestModel().setPassword(randomAlphanumeric(61));
        var weakPassword1 = new CreateUserAccountRequestModel().setPassword("weakpass");
        //profileName
        var nullProfileName = new CreateUserAccountRequestModel().setProfileName(null);
        var blankProfileName = new CreateUserAccountRequestModel().setProfileName("");
        var smallProfileName = new CreateUserAccountRequestModel().setProfileName(randomAlphanumeric(1));
        var bigProfileName = new CreateUserAccountRequestModel().setProfileName(randomAlphanumeric(41));
        var xssAttackProfileName = new CreateUserAccountRequestModel().setProfileName("<script>alert('XSS')</script>");
        var trimmedProfileName1 = new CreateUserAccountRequestModel().setProfileName("  a  ");
        var trimmedProfileName2 = new CreateUserAccountRequestModel().setProfileName("    ");

        return Stream.of(
                //username
                Arguments.of(nullUsername, "username: must not be blank")
                , Arguments.of(blankUsername, "username: must not be blank")
                , Arguments.of(smallUsername, "username: size must be between 3 and 40")
                , Arguments.of(bigUsername, "username: size must be between 3 and 40")
                , Arguments.of(invalidFormatUsername1, "username: invalid format. It should contain only alphanumeric characters, underscore and hyphens")
                , Arguments.of(invalidFormatUsername2, "username: invalid format. It should contain only alphanumeric characters, underscore and hyphens")
                , Arguments.of(invalidFormatUsername3, "username: invalid format. It should contain only alphanumeric characters, underscore and hyphens")
                , Arguments.of(invalidFormatUsername4, "username: invalid format. It should contain only alphanumeric characters, underscore and hyphens")
                , Arguments.of(invalidFormatUsername5, "username: invalid format. It should contain only alphanumeric characters, underscore and hyphens")
                //email
                , Arguments.of(nullEmail, "email: must not be blank")
                , Arguments.of(bigEmail, "email: size must be between 5 and 255")
                , Arguments.of(invalidEmail1, "email: must be a well-formed email address")
                , Arguments.of(invalidEmail2, "email: must be a well-formed email address")
                , Arguments.of(invalidEmail3, "email: must be a well-formed email address")
                //password
                , Arguments.of(nullPassword, "password: must not be blank")
                , Arguments.of(blankPassword, "password: must not be blank")
                , Arguments.of(smallPassword, "password: size must be between 8 and 60")
                , Arguments.of(bigPassword, "password: size must be between 8 and 60")
                , Arguments.of(weakPassword1, "password: does not meet security requirements"
                        , "password: should have at least one lowercase letter"
                        , "password: should have at least one uppercase letter"
                        , "password: should have at least one special character")
                //profileName
                , Arguments.of(nullProfileName, "profileName: must not be blank")
                , Arguments.of(blankProfileName, "profileName: must not be blank")
                , Arguments.of(smallProfileName, "profileName: size must be between 2 and 40")
                , Arguments.of(bigProfileName, "profileName: size must be between 2 and 40")
                , Arguments.of(xssAttackProfileName, "profileName: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
                , Arguments.of(trimmedProfileName1, "profileName: invalid size for trimmed text")
                , Arguments.of(trimmedProfileName2, "profileName: must not be blank")
        );
    }

    static Stream<Arguments> provideValidUserAccountRequest() {
        final String strongPassword1 = "Strong@password1";
        final String strongPassword2 = "St@word1";
        final String validEmail1 = "email@email";
        final String validEmail2 = "a@b.c";
        final String validUsername1 = "User_name-1";
        final String validUsername2 = "User_name-2";
        final String validProfileName1 = " Valid-Profile_Name da Silva ";
        final String validProfileName2 = " Valid -Profile_Name da Silva ";

        var validUsernameModel1 = CreateUserAccountRequestModel.builder().username(validUsername1).password(strongPassword1).email(validEmail1).profileName(validProfileName1).build();
        var validUsernameModel2 = CreateUserAccountRequestModel.builder().username(validUsername2).password(strongPassword2).email(validEmail2).profileName(validProfileName2).build();
        return Stream.of(
                Arguments.of(validUsernameModel1)
                , Arguments.of(validUsernameModel2)
        );
    }

    private static String getSlugFromLocation(String location) {
        String[] split = location.split("/");
        return split[split.length - 1];
    }

    private UserAccount findUserAccountBySlug(String slug) {
        return entityManager.createQuery("SELECT u FROM UserAccount u WHERE u.slug = :slug", UserAccount.class)
                .setParameter("slug", slug)
                .getSingleResult();
    }
}
