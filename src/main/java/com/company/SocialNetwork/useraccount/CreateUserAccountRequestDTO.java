package com.company.SocialNetwork.useraccount;

import com.company.SocialNetwork.shared.validation.SizeTrimmed;
import com.company.SocialNetwork.useraccount.validation.SafeText;
import com.company.SocialNetwork.useraccount.validation.SafeTextType;
import com.company.SocialNetwork.useraccount.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
public class CreateUserAccountRequestDTO {

    @NotBlank
    @Size(min = 2, max = 40)
    @SizeTrimmed(min = 2, max = 40)
    @SafeText
    private String profileName;

    @NotBlank
    @Size(min = 3, max = 40)
    @SafeText(payload = SafeTextType.USERNAME.class)
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
