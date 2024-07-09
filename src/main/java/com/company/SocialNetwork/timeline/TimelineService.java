package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.utils.JsonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneOffset;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    public static final String PUBLIC_TIMELINE_KEY = "publicTimeline";
    private final RedisTemplate<String, String> redisTemplate;

    public void addPostToPublicTimeline(@Valid TimelinePostDTO timelinePost) {
        long postAtInMilli = timelinePost.getPostedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
        redisTemplate.opsForZSet().add(PUBLIC_TIMELINE_KEY, JsonUtils.asJsonString(timelinePost), postAtInMilli);
        log.info("Post added to public timeline: {}", timelinePost);
    }
}