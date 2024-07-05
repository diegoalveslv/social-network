package com.company.SocialNetwork.post;

import com.company.SocialNetwork.exception.FieldValidationException;
import com.company.SocialNetwork.useraccount.UserAccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final UserAccountRepository userAccountRepository;

    public void createPost(@Valid CreatePostRequestDTO requestData) {

        var userSlug = requestData.getUserSlug();
        userAccountRepository.findFirstBySlug(userSlug)
                .orElseThrow(() -> new FieldValidationException(CreatePostRequestDTO.Fields.userSlug, "user not found"));
    }
}
