package com.company.SocialNetwork.timeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LatestPostsDTO {

    private LinkedHashSet<TimelinePostDTO> latestPosts = new LinkedHashSet<>();
    private Double nextScore;
}
