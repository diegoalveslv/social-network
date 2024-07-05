package com.company.SocialNetwork.post;

import com.company.SocialNetwork.TestcontainersConfiguration;
import com.company.SocialNetwork.useraccount.CreateUserAccountRequestDTO;
import com.company.SocialNetwork.useraccount.UserAccountService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.company.SocialNetwork.TestUtils.getSlugFromLocation;
import static com.company.SocialNetwork.post.PostController.CREATE_POST_PATH;
import static com.company.SocialNetwork.utils.JsonUtils.asJsonString;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PostController_CreatePostIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserAccountService userAccountService;

    @Test
    public void givenBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(CREATE_POST_PATH))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCreatePostRequest")
    public void givenInvalidFieldInRequest_shouldReturnUnprocessableEntity(CreatePostValidationRequest request) throws Exception {
        mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(request.getRequestDTO()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItems(request.getExpectedMessages())));
    }

    @Test
    public void givenUserSlugDoesNotExist_shouldReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(CreatePostRequestModel.builder()
                                .text("text")
                                .userSlug("slugnotexist")
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages[0]").value("userSlug: user not found"));
    }

    @Test
    @Sql(scripts = "classpath:db-scripts/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void givenValidRequest_shouldReturnCreated() throws Exception {
        var userSlug = userAccountService.createUserAccount(new CreateUserAccountRequestDTO("profileName", "username", "email@email", "Strong@Pass123"));

        var result = mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(CreatePostRequestModel.builder()
                                .text("text")
                                .userSlug(userSlug)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().exists("location"))
                .andExpect(header().string("location", matchesPattern("http://localhost/posts/\\w{12}")))
                .andReturn();

        String location = result.getResponse().getHeader("location");
        assertThat(location).isNotNull();

        String slug = getSlugFromLocation(location);
        shouldExistInDatabase(slug);

        var post = findPostBySlug(slug);
        shouldHaveSetCreatedDate(post);
    }

    private void shouldHaveSetCreatedDate(Post post) {
        assertThat(post.getCreatedAt()).isNotNull();
    }

    private void shouldExistInDatabase(String slug) {
        try {
            var result = findPostBySlug(slug);
            assertThat(result).isNotNull();
        } catch (NoResultException e) {
            fail("Slug " + slug + " not found in database");
        }
    }

    private Post findPostBySlug(String slug) {
        return entityManager.createQuery("SELECT p FROM Post p WHERE p.slug = :slug", Post.class)
                .setParameter("slug", slug)
                .getSingleResult();
    }

    static Stream<CreatePostValidationRequest> provideInvalidCreatePostRequest() {
        //text
        var nullText = CreatePostRequestModel.builder().text(null).build();
        var emptyText = CreatePostRequestModel.builder().text("").build();
        var smallText = CreatePostRequestModel.builder().text("a").build();
        var bigText = CreatePostRequestModel.builder().text(randomAlphanumeric(501)).build();
        var notTrimmedText = CreatePostRequestModel.builder().text("     ").build();
        var notTrimmedText2 = CreatePostRequestModel.builder().text("  a  ").build();
        var xssAttackText = CreatePostRequestModel.builder().text("<script>alert('XSS')</script>").build();
        //userSlug
        var nullUserSlug = CreatePostRequestModel.builder().userSlug(null).build();
        var emptyUserSlug = CreatePostRequestModel.builder().userSlug("").build();
        var smallUserSlug = CreatePostRequestModel.builder().userSlug(randomAlphanumeric(11)).build();
        var bigUserSlug = CreatePostRequestModel.builder().userSlug(randomAlphanumeric(13)).build();
        var notTrimmedUserSlug = CreatePostRequestModel.builder().userSlug("            ").build();
        var notTrimmedUserSlug2 = CreatePostRequestModel.builder().userSlug("      e     ").build();

        return Stream.of(
                //text
                CreatePostValidationRequest.of(nullText, "text: must not be blank")
                , CreatePostValidationRequest.of(emptyText, "text: must not be blank")
                , CreatePostValidationRequest.of(smallText, "text: size must be between 2 and 500")
                , CreatePostValidationRequest.of(bigText, "text: size must be between 2 and 500")
                , CreatePostValidationRequest.of(notTrimmedText, "text: must not be blank")
                , CreatePostValidationRequest.of(notTrimmedText2, "text: invalid size for trimmed text")
                , CreatePostValidationRequest.of(xssAttackText, "text: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
                //userSlug
                , CreatePostValidationRequest.of(nullUserSlug, "userSlug: must not be blank")
                , CreatePostValidationRequest.of(emptyUserSlug, "userSlug: must not be blank")
                , CreatePostValidationRequest.of(smallUserSlug, "userSlug: size must be between 12 and 12")
                , CreatePostValidationRequest.of(bigUserSlug, "userSlug: size must be between 12 and 12")
                , CreatePostValidationRequest.of(notTrimmedUserSlug, "userSlug: must not be blank")
                , CreatePostValidationRequest.of(notTrimmedUserSlug2, "userSlug: invalid size for trimmed text")
        );
    }
}