package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.exception.FieldValidationException;
import com.company.SocialNetwork.shared.PublicTimelineResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

        nextScoreDouble.ifPresent(TimelineService::validateNextScore);
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

    public Long countNewPosts(String nextScore) {
        Optional<Double> nextScoreDouble = convertNextScoreToDoubleValue(nextScore);
        Double nextScoreValue = nextScoreDouble.orElseThrow(() -> new IllegalArgumentException("nextScore cannot be empty"));

        validateNextScore(nextScoreValue);

        return timelineRepository.countNewPosts(nextScoreValue);
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

    private static void validateNextScore(Double nextScoreValue) {
        long nowInMilli = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        if (nextScoreValue.longValue() > nowInMilli) {
            throw new FieldValidationException("nextScore", "invalid value. Please request without it to get the latest scores.");
        }

        if (nextScoreValue.longValue() <= 0) {
            throw new FieldValidationException("nextScore", "value should be greater than zero. Please request without it to get the latest scores.");
        }
    }
}