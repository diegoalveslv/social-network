package com.company.SocialNetwork.post;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PostService {

    public void createPost(@Valid CreatePostRequestDTO requestData) {

    }
}
