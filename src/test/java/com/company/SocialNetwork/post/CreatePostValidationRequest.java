package com.company.SocialNetwork.post;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostValidationRequest {
    private CreatePostRequestModel requestDTO;
    private String[] expectedMessages;

    public static CreatePostValidationRequest of(CreatePostRequestModel requestDTO, String... expectedMessages) {
        return CreatePostValidationRequest.builder()
                .requestDTO(requestDTO)
                .expectedMessages(expectedMessages)
                .build();
    }
}
