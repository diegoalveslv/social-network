package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.TestcontainersConfigurationPostgres;
import com.company.SocialNetwork.TestcontainersConfigurationRedis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;

import static com.company.SocialNetwork.timeline.TimelineController.COUNT_NEW_POSTS_PATH;
import static java.time.ZoneOffset.UTC;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfigurationRedis.class, TestcontainersConfigurationPostgres.class})
//TODO postgres is not needed here but I added it to be able to load the context. Fix this
public class TimelineController_CountNewPostsIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TimelineService timelineService;

    @Test
    public void givenEmptyNextScoreRequestParameter_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(COUNT_NEW_POSTS_PATH))
                .andExpect(status().isBadRequest());
    }

    @Nested
    class WithRedisCache {

        @BeforeEach
        public void tearDown() {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }

        @Test
        public void givenEmptyTimeline_shouldReturnZeroCount() throws Exception {
            mockMvc.perform(get(COUNT_NEW_POSTS_PATH)
                            .queryParam("nextScore", String.valueOf(1D)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }

        @Test
        public void givenNextScoreIsGreaterThanMaxScorePossible_shouldReturnUnprocessableEntity() throws Exception {
            Double invalidNextScoreValue = (double) ZonedDateTime.now(UTC).plusDays(1).toInstant().toEpochMilli();
            mockMvc.perform(get(COUNT_NEW_POSTS_PATH)
                            .queryParam("nextScore", String.valueOf(invalidNextScoreValue)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.messages[0]").value("nextScore: invalid value. Please request without it to get the latest scores."));
        }

        @Test
        public void givenNextScoreIsNegative_shouldReturnUnprocessableEntity() throws Exception {
            Double negativeNextScoreValue = -1D;
            mockMvc.perform(get(COUNT_NEW_POSTS_PATH).param("nextScore", String.valueOf(negativeNextScoreValue)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.messages[0]").value("nextScore: value should be greater than zero. Please request without it to get the latest scores."));
        }

        @Test
        public void givenOnePostInTimeline_shouldReturnOkAndCorrectValues() throws Exception {
            var postedAt = ZonedDateTime.now();
            var timelinePost = TimelinePostDTO.builder()
                    .postSlug("postSlug")
                    .userSlug("userSlug")
                    .username("username")
                    .profileName("profileName")
                    .content("content")
                    .postedAt(postedAt)
                    .build();

            timelineService.addPostToPublicTimeline(timelinePost);

            double nowMinusInMinuteTimestamp = ZonedDateTime.now().minusMinutes(1).toInstant().toEpochMilli();
            mockMvc.perform(get(COUNT_NEW_POSTS_PATH).param("nextScore", String.valueOf(nowMinusInMinuteTimestamp)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(1))
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }

        @Test
        public void givenTwoPostsAndFilterOnlyOneByScore_shouldReturnTotalCountAsOne() throws Exception {
            var latestPost = TimelinePostDTO.builder()
                    .postSlug("postSlug0")
                    .userSlug("userSlug")
                    .username("username")
                    .profileName("profileName")
                    .content("content")
                    .postedAt(ZonedDateTime.now()) // newest post is now
                    .build();

            timelineService.addPostToPublicTimeline(latestPost);
            ZonedDateTime oldestPostDate = ZonedDateTime.now().minusDays(1);
            var olderPost = latestPost.toBuilder().postSlug("postSlug1").postedAt(oldestPostDate).build();
            timelineService.addPostToPublicTimeline(olderPost);

            long oneMinuteAfterOldestPost = oldestPostDate.plusMinutes(1).toInstant().toEpochMilli();
            mockMvc.perform(get(COUNT_NEW_POSTS_PATH).param("nextScore", String.valueOf(oneMinuteAfterOldestPost)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(1)) // counted the newest post
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }
    }

}