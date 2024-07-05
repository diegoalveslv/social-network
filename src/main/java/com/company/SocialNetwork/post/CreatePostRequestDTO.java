package com.company.SocialNetwork.post;

import com.company.SocialNetwork.shared.validation.NotBlankTrimmed;
import com.company.SocialNetwork.shared.validation.SizeTrimmed;
import com.company.SocialNetwork.useraccount.validation.SafeText;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class CreatePostRequestDTO {

    @NotBlankTrimmed
    @Size(min = 12, max = 12)
    @SizeTrimmed(min = 12, max = 12)
    private String userSlug;

    @NotBlankTrimmed
    @Size(min = 2, max = 500)
    @SizeTrimmed(min = 2, max = 500)
    @SafeText
    //TODO in this case this could be less strict. Check if I can use the groups variable in the interface to change the strictness
    //TODO rename this to content to match de entity
    private String text;

}
