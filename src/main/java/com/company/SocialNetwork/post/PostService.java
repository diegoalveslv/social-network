package com.company.SocialNetwork.post;

import com.company.SocialNetwork.exception.FieldValidationException;
import com.company.SocialNetwork.exception.NotFoundException;
import com.company.SocialNetwork.shared.validation.SizeTrimmed;
import com.company.SocialNetwork.useraccount.UserAccount;
import com.company.SocialNetwork.useraccount.UserAccountRepository;
import com.company.SocialNetwork.utils.SlugGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;
    private final SlugGenerator slugGenerator;

    public String createPost(@Valid CreatePostRequestDTO requestData) {
        var userSlug = requestData.getUserSlug();
        var user = getUserOrThrowException(userSlug, CreatePostRequestDTO.Fields.userSlug);
        var content = formatContent(requestData.getContent());

        var post = Post.builder()
                .slug(slugGenerator.generateSlug())
                .userAccount(user)
                .content(content)
                .createdAt(ZonedDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getSlug();
    }

    public String commentPost(@Valid @NotBlank @Size(min = 12, max = 12) @SizeTrimmed(min = 12, max = 12) String postSlug //TODO ugly
            , @Valid CommentPostRequestDTO requestData) {
        var postCommentedOn = getPostOrThrowNotFound(postSlug);
        var user = getUserOrThrowException(requestData.getUserSlug(), CommentPostRequestDTO.Fields.userSlug);
        var content = formatContent(requestData.getContent());

        var postComment = Post.builder()
                .slug(slugGenerator.generateSlug())
                .commentToPost(postCommentedOn)
                .userAccount(user)
                .content(content)
                .createdAt(ZonedDateTime.now())
                .build();

        Post savedPostComment = postRepository.save(postComment);

        return savedPostComment.getSlug();
    }

    private Post getPostOrThrowNotFound(String commentToPostSlug) {
        return postRepository.findFirstBySlug(commentToPostSlug).orElseThrow(() -> new NotFoundException("post not found"));
    }

    private UserAccount getUserOrThrowException(String userSlug, String fieldName) {
        return userAccountRepository.findFirstBySlug(userSlug)
                .orElseThrow(() -> new FieldValidationException(fieldName, "user not found"));
    }

    private String formatContent(String content) {
        content = content.replaceAll("\u0000", "");
        content = StringEscapeUtils.escapeHtml4(content);
        return content.trim();
    }
}
