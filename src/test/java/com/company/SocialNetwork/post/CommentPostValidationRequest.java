package com.company.SocialNetwork.post;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentPostValidationRequest {
    private CommentPostRequestModel requestDTO;
    private String[] expectedMessages;

    public static CommentPostValidationRequest of(CommentPostRequestModel requestDTO, String... expectedMessages) {
        return CommentPostValidationRequest.builder()
                .requestDTO(requestDTO)
                .expectedMessages(expectedMessages)
                .build();
    }
}