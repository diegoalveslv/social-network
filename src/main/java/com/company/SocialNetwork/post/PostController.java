package com.company.SocialNetwork.post;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class PostController {

    public static final String CREATE_POST_PATH = "/posts";
    public static final String COMMENT_POST_PATH = "/posts/{postSlug}/comment";

    private final PostService postService;

    @PostMapping(CREATE_POST_PATH)
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequestDTO requestData) {

        String postSlug = postService.createPost(requestData);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{postSlug}")
                .buildAndExpand(postSlug)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping(COMMENT_POST_PATH)
    public ResponseEntity<?> commentPost(@PathVariable String postSlug, @RequestBody CommentPostRequestDTO requestData) {

        String commentPostSlug = postService.commentPost(postSlug, requestData);

        URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping()
                .path("/posts/{postSlug}")
                .buildAndExpand(commentPostSlug)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
