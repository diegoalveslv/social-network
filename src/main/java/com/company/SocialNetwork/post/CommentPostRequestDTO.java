package com.company.SocialNetwork.post;

import com.company.SocialNetwork.shared.validation.SizeTrimmed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class CommentPostRequestDTO {

    @NotBlank
    @Size(min = 12, max = 12)
    @SizeTrimmed(min = 12, max = 12)
    private String userSlug;

    @NotBlank
    @Size(min = 2, max = 500)
    @SizeTrimmed(min = 2, max = 500)
    private String content;

}
