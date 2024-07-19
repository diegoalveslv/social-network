package com.company.SocialNetwork.shared;

import com.company.SocialNetwork.timeline.TimelinePostDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicTimelineResponseDTO {

    @Builder.Default
    private Set<TimelinePostDTO> content = new HashSet<>();

    @Builder.Default
    private String totalItems = "*";

    private String nextScore;
}
