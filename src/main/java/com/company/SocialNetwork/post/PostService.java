package com.company.SocialNetwork.post;

import com.company.SocialNetwork.exception.FieldValidationException;
import com.company.SocialNetwork.useraccount.UserAccountRepository;
import com.company.SocialNetwork.utils.SlugGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        var user = userAccountRepository.findFirstBySlug(userSlug)
                .orElseThrow(() -> new FieldValidationException(CreatePostRequestDTO.Fields.userSlug, "user not found"));

        var post = Post.builder()
                .slug(slugGenerator.generateSlug())
                .userAccount(user)
                .content(requestData.getContent())
                .createdAt(ZonedDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getSlug();
    }
}
