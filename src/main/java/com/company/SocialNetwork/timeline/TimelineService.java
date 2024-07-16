package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.exception.FieldValidationException;
import com.company.SocialNetwork.shared.PublicTimelineResponseDTO;
import com.company.SocialNetwork.utils.JsonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    public static final String PUBLIC_TIMELINE_KEY = "publicTimeline";
    private final static int MAX_PAGE_SIZE = 10;
    private final RedisTemplate<String, String> redisTemplate;

    public void addPostToPublicTimeline(@Valid TimelinePostDTO timelinePost) {
        timelinePost.setPostedAt(timelinePost.getPostedAt().withZoneSameInstant(ZoneOffset.UTC));

        double postAtInMilli = getPostScore(timelinePost);

        redisTemplate.opsForZSet().add(PUBLIC_TIMELINE_KEY, JsonUtils.asJsonString(timelinePost), postAtInMilli);
        log.info("Post added to public timeline: {}", timelinePost);
    }

    public PublicTimelineResponseDTO<TimelinePostDTO> readPublicTimeline(Double nextScore) throws FieldValidationException {

        Set<ZSetOperations.TypedTuple<String>> reverseRange = getLatestPosts(nextScore);

        if (reverseRange == null) {
            return new PublicTimelineResponseDTO<>();
        }

        LinkedHashSet<TimelinePostDTO> timelinePosts = reverseRange.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(postAsJson -> JsonUtils.readValue(postAsJson, TimelinePostDTO.class))
                .limit(MAX_PAGE_SIZE)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String totalItems = getTotalItemsString();
        Double newNextScore = getNextScoreFromRangeIfPossible(reverseRange);

        var response = new PublicTimelineResponseDTO<TimelinePostDTO>();
        response.setContent(timelinePosts);
        response.setTotalItems(totalItems);
        response.setNextScore(newNextScore);

        return response;
    }

    public double getPostScore(TimelinePostDTO timelinePost) {
        return (double) timelinePost.getPostedAt().toInstant().toEpochMilli();
    }

    private Set<ZSetOperations.TypedTuple<String>> getLatestPosts(Double nextScore) {

        Set<ZSetOperations.TypedTuple<String>> reverseRange;
        long nowInMilli = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        if (nextScore == null) {
            reverseRange = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(PUBLIC_TIMELINE_KEY, 0, nowInMilli, 0, MAX_PAGE_SIZE + 1);
        } else {
            if (nextScore.longValue() > nowInMilli) {
                throw new FieldValidationException("nextScore", "invalid value. Please request without it to get the latest scores.");
            }

            if (nextScore.longValue() <= 0) {
                throw new FieldValidationException("nextScore", "value should be greater than zero. Please request without it to get the latest scores.");
            }

            reverseRange = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(PUBLIC_TIMELINE_KEY, 0, nextScore, 0, MAX_PAGE_SIZE + 1);
        }

        return reverseRange;
    }

    private static Double getNextScoreFromRangeIfPossible(Set<ZSetOperations.TypedTuple<String>> reverseRange) {
        Double newNextScore = null;
        if (reverseRange.size() > MAX_PAGE_SIZE) {
            ZSetOperations.TypedTuple<String> lastItem = (ZSetOperations.TypedTuple<String>) reverseRange.toArray()[reverseRange.size() - 1];
            newNextScore = Objects.requireNonNull(lastItem.getScore());
        }
        return newNextScore;
    }

    private String getTotalItemsString() {
        Long totalItems = redisTemplate.opsForZSet().zCard(PUBLIC_TIMELINE_KEY);
        return totalItems != null ? String.valueOf(totalItems) : "*";
    }
}