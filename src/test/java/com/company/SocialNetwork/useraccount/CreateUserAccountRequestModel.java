package com.company.SocialNetwork.useraccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserAccountRequestModel {
    private String profileName;
    private String username;
    private String email;
    private String password;
}
