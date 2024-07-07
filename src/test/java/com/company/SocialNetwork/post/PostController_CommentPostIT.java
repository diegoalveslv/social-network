package com.company.SocialNetwork.post;

import com.company.SocialNetwork.TestcontainersConfiguration;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Stream;

import static com.company.SocialNetwork.TestUtils.getSlugFromLocation;
import static com.company.SocialNetwork.post.PostController.COMMENT_POST_PATH;
import static com.company.SocialNetwork.utils.JsonUtils.asJsonString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PostController_CommentPostIT {

    private final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(COMMENT_POST_PATH);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PostService postService;

    @Test
    public void givenBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        String url = uriBuilder.buildAndExpand("postSlug").toUriString();
        mockMvc.perform(post(url))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPostSlugs")
    public void givenInvalidPostSlugInPathParam_thenReturnUnprocessableEntity(String postSlug, String expectedMessage) throws Exception {
        String url = uriBuilder.buildAndExpand(postSlug).toUriString();
        mockMvc.perform(post(url)
                        .content(asJsonString(CommentPostRequestModel.builder()
                                .content("text")
                                .userSlug("slugnotexist")
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItem(expectedMessage)));
    }

    @Test
    public void givenPostSlugInPathParamDoesNotExist_thenReturnNotFound() throws Exception {
        String url = uriBuilder.buildAndExpand("slugnotexist").toUriString();
        mockMvc.perform(post(url)
                        .content(asJsonString(CommentPostRequestModel.builder()
                                .content("text")
                                .userSlug("slugnotexist")
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value("post not found"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCommentPostRequest")
    public void givenInvalidFieldInRequest_shouldReturnUnprocessableEntity(CommentPostRequestModel request, String expectedMessage) throws Exception {
        String url = uriBuilder.buildAndExpand("postSlug").toUriString();
        mockMvc.perform(post(url)
                        .content(asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItems(expectedMessage)));
    }

    @Test
    @Sql(scripts = "classpath:db-scripts/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void givenUserSlugDoesNotExist_shouldReturnUnprocessableEntity() throws Exception {
        var userSlug = createValidUserAccount();
        var postSlug = createValidPost(userSlug);
        String url = uriBuilder.buildAndExpand(postSlug).toUriString();
        mockMvc.perform(post(url)
                        .content(asJsonString(CommentPostRequestModel.builder()
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
        var userSlug = createValidUserAccount();
        var postSlug = createValidPost(userSlug);
        String url = uriBuilder.buildAndExpand(postSlug).toUriString();

        var result = mockMvc.perform(post(url)
                        .content(asJsonString(CommentPostRequestModel.builder()
                                .content("comment text")
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

        var postComment = findPostBySlug(slug);
        shouldHaveSetCreatedDate(postComment);
        shouldHaveSetCommentOnPost(postComment, postSlug);
    }

    @Test
    @Sql(scripts = "classpath:db-scripts/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void givenPostContentHasSpecialCharacters_shouldSaveEscapedVersionOfContent() throws Exception {
        var userSlug = createValidUserAccount();
        var postSlug = createValidPost(userSlug);
        String url = uriBuilder.buildAndExpand(postSlug).toUriString();
        var unescapedText = "<>\"'&\\/<>'\"&=+-()[]{};, \t\n\r\u0000";
        var escapedText = "&lt;&gt;&quot;'&amp;\\/&lt;&gt;'&quot;&amp;=+-()[]{};, \t\n\r";

        var result = mockMvc.perform(post(url)
                        .content(asJsonString(CommentPostRequestModel.builder()
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

    private void shouldHaveContentEqualTo(Post post, String specialCharacters) {
        assertThat(post.getContent()).isEqualTo(specialCharacters);
    }

    private void shouldHaveSetCommentOnPost(Post postComment, String postSlug) {
        assertThat(postComment.getCommentToPost().getSlug()).isEqualTo(postSlug);
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

    static Stream<Arguments> provideInvalidPostSlugs() {
        return Stream.of(
                Arguments.of(randomAlphanumeric(11), "postSlug: size must be between 12 and 12")
                , Arguments.of(randomAlphanumeric(13), "postSlug: size must be between 12 and 12")
                , Arguments.of("            ", "postSlug: must not be blank")
                , Arguments.of("      e     ", "postSlug: invalid size for trimmed text"
                ));
    }

    static Stream<Arguments> provideInvalidCommentPostRequest() {
        //text
        var nullText = new CommentPostRequestModel().setContent(null);
        var emptyText = new CommentPostRequestModel().setContent("");
        var smallText = new CommentPostRequestModel().setContent("a");
        var bigText = new CommentPostRequestModel().setContent(randomAlphanumeric(501));
        var notTrimmedText = new CommentPostRequestModel().setContent("      ");
        var notTrimmedText2 = new CommentPostRequestModel().setContent("  a   ");

        //userSlug
        var nullUserSlug = new CommentPostRequestModel().setUserSlug(null);
        var emptyUserSlug = new CommentPostRequestModel().setUserSlug("");
        var smallUserSlug = new CommentPostRequestModel().setUserSlug(randomAlphanumeric(11));
        var bigUserSlug = new CommentPostRequestModel().setUserSlug(randomAlphanumeric(13));
        var notTrimmedUserSlug = new CommentPostRequestModel().setUserSlug("             ");
        var notTrimmedUserSlug2 = new CommentPostRequestModel().setUserSlug("      e      ");

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

    private Post findPostBySlug(String slug) {
        return entityManager.createQuery("SELECT p FROM Post p WHERE p.slug = :slug", Post.class)
                .setParameter("slug", slug)
                .getSingleResult();
    }

    private String createValidUserAccount() {
        return userAccountService.createUserAccount(new CreateUserAccountRequestDTO("profileName", "username", "email@email", "Strong@Pass123"));
    }

    private String createValidPost(String userSlug) {
        return postService.createPost(new CreatePostRequestDTO(userSlug, "text"));
    }
}