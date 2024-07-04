package com.company.SocialNetwork.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserAccountValidationRequest {
    private CreateUserAccountRequestModel requestDTO;
    private String[] expectedMessages;

    public static CreateUserAccountValidationRequest of(CreateUserAccountRequestModel requestDTO, String... expectedMessage) {
        return CreateUserAccountValidationRequest.builder()
                .requestDTO(requestDTO)
                .expectedMessages(expectedMessage)
                .build();
    }
}
