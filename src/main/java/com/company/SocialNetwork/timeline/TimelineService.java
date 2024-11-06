package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.exception.FieldValidationException;
import com.company.SocialNetwork.shared.PublicTimelineResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    private final TimelineRepository timelineRepository;

    public void addPostToPublicTimeline(@Valid TimelinePostDTO timelinePost) {
        timelinePost.setPostedAt(timelinePost.getPostedAt().withZoneSameInstant(ZoneOffset.UTC));

        timelineRepository.addPostToPublicTimeline(timelinePost);
        log.info("Post added to public timeline: {}", timelinePost);
    }

    public PublicTimelineResponseDTO readPublicTimeline(String nextScore) throws FieldValidationException {
        Optional<Double> nextScoreDouble = convertNextScoreToDoubleValue(nextScore);

        LatestPostsDTO latestPosts = timelineRepository.getLatestPosts(nextScoreDouble);

        String totalItems = timelineRepository.getTotalItemsString();
        if (latestPosts.getLatestPosts().isEmpty()) {
            PublicTimelineResponseDTO emptyResponse = new PublicTimelineResponseDTO();
            emptyResponse.setTotalItems(totalItems);
            return emptyResponse;
        }

        return PublicTimelineResponseDTO.builder()
                .content(latestPosts.getLatestPosts())
                .totalItems(totalItems)
                .nextScore(latestPosts.getNextScore() != null ? String.valueOf(latestPosts.getNextScore()) : null)
                .build();
    }

    private static Optional<Double> convertNextScoreToDoubleValue(String nextScore) {
        Double nextScoreDouble = null;
        if (nextScore != null) {
            try {
                nextScoreDouble = Double.parseDouble(nextScore);
            } catch (Exception e) {
                log.error("Unable to convert score `%s` to double".formatted(nextScore), e);
                throw new FieldValidationException("nextScore", "invalid value. Cannot be converted to double.");
            }
        }
        return Optional.ofNullable(nextScoreDouble);
    }
}