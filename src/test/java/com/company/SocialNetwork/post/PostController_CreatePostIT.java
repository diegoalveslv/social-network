package com.company.SocialNetwork.post;

import com.company.SocialNetwork.TestcontainersConfigurationPostgres;
import com.company.SocialNetwork.useraccount.CreateUserAccountRequestDTO;
import com.company.SocialNetwork.useraccount.UserAccountService;
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
@Import(TestcontainersConfigurationPostgres.class)
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
    public void givenInvalidFieldInRequest_shouldReturnUnprocessableEntity(CreatePostRequestModel request, String expectedMessage) throws Exception {
        mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItems(expectedMessage)));
    }

    @Test
    public void givenUserSlugDoesNotExist_shouldReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(CreatePostRequestModel.builder()
                                .content("text")
                                .userSlug("slugnotexist")
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages[0]").value("userSlug: user not found"));
    }

    @Test
    @Sql(scripts = "classpath:db-scripts/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void givenValidRequest_shouldReturnCreated() throws Exception {
        var userSlug = createValidUser();

        var content = " text ";
        var result = mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(CreatePostRequestModel.builder()
                                .content(content)
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
        shouldHaveTrimmedContent(post, content);
    }

    @Test
    @Sql(scripts = "classpath:db-scripts/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void givenPostContentHasSpecialCharacters_shouldSaveEscapedVersionOfContent() throws Exception {
        var userSlug = createValidUser();
        var unescapedText = "<>\"'&\\/<>'\"&=+-()[]{};, \t\n\r\u0000end";
        var escapedText = "&lt;&gt;&quot;'&amp;\\/&lt;&gt;'&quot;&amp;=+-()[]{};, \t\n\rend";

        var result = mockMvc.perform(post(CREATE_POST_PATH)
                        .content(asJsonString(CreatePostRequestModel.builder()
                                .content(unescapedText)
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
        shouldHaveContentEqualTo(post, escapedText);
    }

    private void shouldHaveTrimmedContent(Post post, String notTrimmedContent) {
        assertThat(post.getContent()).isEqualTo(notTrimmedContent.trim());
    }

    private void shouldHaveContentEqualTo(Post post, String specialCharacters) {
        assertThat(post.getContent()).isEqualTo(specialCharacters);
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

    static Stream<Arguments> provideInvalidCreatePostRequest() {
        //text
        var nullText = new CreatePostRequestModel().setContent(null);
        var emptyText = new CreatePostRequestModel().setContent("");
        var smallText = new CreatePostRequestModel().setContent("a");
        var bigText = new CreatePostRequestModel().setContent(randomAlphanumeric(501));
        var notTrimmedText = new CreatePostRequestModel().setContent("      ");
        var notTrimmedText2 = new CreatePostRequestModel().setContent("  a   ");
        //userSlug
        var nullUserSlug = new CreatePostRequestModel().setUserSlug(null);
        var emptyUserSlug = new CreatePostRequestModel().setUserSlug("");
        var smallUserSlug = new CreatePostRequestModel().setUserSlug(randomAlphanumeric(11));
        var bigUserSlug = new CreatePostRequestModel().setUserSlug(randomAlphanumeric(13));
        var notTrimmedUserSlug = new CreatePostRequestModel().setUserSlug("             ");
        var notTrimmedUserSlug2 = new CreatePostRequestModel().setUserSlug("      e      ");

        return Stream.of(
                //text
                Arguments.of(nullText, "content: must not be blank")
                , Arguments.of(emptyText, "content: must not be blank")
                , Arguments.of(smallText, "content: size must be between 2 and 500")
                , Arguments.of(bigText, "content: size must be between 2 and 500")
                , Arguments.of(notTrimmedText, "content: must not be blank")
                , Arguments.of(notTrimmedText2, "content: invalid size for trimmed text")
                //userSlug
                , Arguments.of(nullUserSlug, "userSlug: must not be blank")
                , Arguments.of(emptyUserSlug, "userSlug: must not be blank")
                , Arguments.of(smallUserSlug, "userSlug: size must be between 12 and 12")
                , Arguments.of(bigUserSlug, "userSlug: size must be between 12 and 12")
                , Arguments.of(notTrimmedUserSlug, "userSlug: must not be blank")
                , Arguments.of(notTrimmedUserSlug2, "userSlug: invalid size for trimmed text")
        );
    }

    private String createValidUser() {
        return userAccountService.createUserAccount(new CreateUserAccountRequestDTO("profileName", "username", "email@email", "Strong@Pass123"));
    }

    private Post findPostBySlug(String slug) {
        return entityManager.createQuery("SELECT p FROM Post p WHERE p.slug = :slug", Post.class)
                .setParameter("slug", slug)
                .getSingleResult();
    }
}