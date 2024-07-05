package com.company.SocialNetwork.post;

import com.company.SocialNetwork.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.company.SocialNetwork.utils.JsonUtils.asJsonString;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PostController_CreatePostIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/posts"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCreatePostRequest")
    public void givenInvalidFieldInRequest_shouldReturnUnprocessableEntity(CreatePostValidationRequest request) throws Exception {
        mockMvc.perform(post("/posts")
                        .content(asJsonString(request.getRequestDTO()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messages").value(hasItems(request.getExpectedMessages())));
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
        return Stream.of(
                //text
                CreatePostValidationRequest.of(nullText, "text: must not be blank")
                , CreatePostValidationRequest.of(emptyText, "text: must not be blank")
                , CreatePostValidationRequest.of(smallText, "text: size must be between 2 and 500")
                , CreatePostValidationRequest.of(bigText, "text: size must be between 2 and 500")
                , CreatePostValidationRequest.of(notTrimmedText, "text: must not be blank")
                , CreatePostValidationRequest.of(notTrimmedText2, "text: invalid size for trimmed text")
                , CreatePostValidationRequest.of(xssAttackText, "text: invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens")
        );
    }
}