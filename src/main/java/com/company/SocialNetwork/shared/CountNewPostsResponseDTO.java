package com.company.SocialNetwork.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountNewPostsResponseDTO {

    @Builder.Default
    private String totalItems = "*";

    private String nextScore;
}
