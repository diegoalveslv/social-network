package com.company.SocialNetwork.user;

import com.company.SocialNetwork.user.validation.ValidPassword;
import com.company.SocialNetwork.user.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserAccountRequestDTO {

    @NotBlank
    @Size(min = 2, max = 40)
    private String profileName;

    @NotBlank
    @Size(min = 3, max = 40)
    @ValidUsername
    private String username;

    @NotBlank
    @Email
    @Size(min = 5, max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 60)
    @ValidPassword
    private String password;
}
