package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineRedisRepository implements TimelineRepository {

    public static final String PUBLIC_TIMELINE_KEY = "publicTimeline";
    private final static int MAX_PAGE_SIZE = 10;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public LatestPostsDTO getLatestPosts(Optional<Double> nextScore) {
        Set<ZSetOperations.TypedTuple<String>> reverseRange;
        if (nextScore.isEmpty()) {
            long nowInMilli = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
            reverseRange = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(PUBLIC_TIMELINE_KEY, 0, nowInMilli, 0, MAX_PAGE_SIZE + 1);
        } else {
            Double nextScoreValue = nextScore.get();
            reverseRange = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(PUBLIC_TIMELINE_KEY, 0, nextScoreValue, 0, MAX_PAGE_SIZE + 1);
        }

        if (reverseRange == null) {
            return new LatestPostsDTO();
        }

        LinkedHashSet<TimelinePostDTO> content = reverseRange.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(postAsJson -> JsonUtils.readValue(postAsJson, TimelinePostDTO.class))
                .limit(MAX_PAGE_SIZE)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Double newNextScore = getNextScoreFromRangeIfPossible(reverseRange);

        return new LatestPostsDTO(content, newNextScore);
    }

    @Override
    public String getTotalItemsString() {
        Long totalItems = redisTemplate.opsForZSet().zCard(PUBLIC_TIMELINE_KEY);
        return totalItems != null ? String.valueOf(totalItems) : "*";
    }

    @Override
    public void addPostToPublicTimeline(TimelinePostDTO timelinePost) {
        double postAtInMilli = getPostScore(timelinePost);
        redisTemplate.opsForZSet().add(PUBLIC_TIMELINE_KEY, JsonUtils.asJsonString(timelinePost), postAtInMilli);
    }

    @Override
    public Long countNewPosts(Double startingFromScore) {
        return redisTemplate.opsForZSet().count(PUBLIC_TIMELINE_KEY, startingFromScore, Double.MAX_VALUE);
    }

    private double getPostScore(TimelinePostDTO timelinePost) {
        return (double) timelinePost.getPostedAt().toInstant().toEpochMilli();
    }

    private static Double getNextScoreFromRangeIfPossible(Set<ZSetOperations.TypedTuple<String>> reverseRange) {
        Double newNextScore = null;
        if (reverseRange.size() > MAX_PAGE_SIZE) {
            ZSetOperations.TypedTuple<String> lastItem = (ZSetOperations.TypedTuple<String>) reverseRange.toArray()[reverseRange.size() - 1];
            newNextScore = Objects.requireNonNull(lastItem.getScore());
        }
        return newNextScore;
    }
}
