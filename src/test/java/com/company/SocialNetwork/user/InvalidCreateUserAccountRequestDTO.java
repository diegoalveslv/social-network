package com.company.SocialNetwork.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvalidCreateUserAccountRequestDTO {
    private CreateUserAccountRequestModel requestDTO;
    private String[] expectedMessages;

    public static InvalidCreateUserAccountRequestDTO of(CreateUserAccountRequestModel requestDTO, String... expectedMessage) {
        return InvalidCreateUserAccountRequestDTO.builder()
                .requestDTO(requestDTO)
                .expectedMessages(expectedMessage)
                .build();
    }
}
