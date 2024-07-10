package com.company.SocialNetwork.timeline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TimelinePostDTO {

    @NotNull
    @NotBlank
    private String postSlug;

    @NotNull
    @NotBlank
    private String userSlug;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String profileName;

    @NotNull
    @NotBlank
    private String content;

    @NotNull
    private ZonedDateTime postedAt;
}
