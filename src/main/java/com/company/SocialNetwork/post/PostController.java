package com.company.SocialNetwork.post;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class PostController {

    public static final String CREATE_POST_PATH = "/posts";

    private final PostService postService;

    @PostMapping(CREATE_POST_PATH)
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequestDTO requestData) {

        String postSlug = postService.createPost(requestData);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{slug}")
                .buildAndExpand(postSlug)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
