package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.TestcontainersConfigurationPostgres;
import com.company.SocialNetwork.TestcontainersConfigurationRedis;
import com.company.SocialNetwork.shared.PublicTimelineResponseDTO;
import com.company.SocialNetwork.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.List;

import static com.company.SocialNetwork.timeline.TimelineController.READ_TIMELINE_PATH;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfigurationRedis.class, TestcontainersConfigurationPostgres.class})
//TODO postgres is not needed here but I added it to be able to load the context. Fix this
//TODO version the APIs
public class TimelineController_ReadPublicTimelineIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TimelineService timelineService;

    @Nested
    class WithRedisCache {

        @BeforeEach
        public void tearDown() {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }

        @Test
        public void givenEmptyTimeline_shouldReturnOk() throws Exception {
            mockMvc.perform(get(READ_TIMELINE_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }

        @Test
        public void givenNextScoreIsGreaterThanMaxScorePossible_shouldReturnUnprocessableEntity() throws Exception {
            Double invalidNextScoreValue = (double) ZonedDateTime.now(UTC).plusDays(1).toInstant().toEpochMilli();
            mockMvc.perform(get(READ_TIMELINE_PATH).param("nextScore", String.valueOf(invalidNextScoreValue)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.messages[0]").value("nextScore: invalid value. Please request without it to get the latest scores."));
        }

        @Test
        public void givenNextScoreIsNegative_shouldReturnUnprocessableEntity() throws Exception {
            Double negativeNextScoreValue = -1D;
            mockMvc.perform(get(READ_TIMELINE_PATH).param("nextScore", String.valueOf(negativeNextScoreValue)))
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

            mockMvc.perform(get(READ_TIMELINE_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].postSlug").value(timelinePost.getPostSlug()))
                    .andExpect(jsonPath("$.content[0].userSlug").value(timelinePost.getUserSlug()))
                    .andExpect(jsonPath("$.content[0].username").value(timelinePost.getUsername()))
                    .andExpect(jsonPath("$.content[0].profileName").value(timelinePost.getProfileName()))
                    .andExpect(jsonPath("$.content[0].content").value(timelinePost.getContent()))
                    .andExpect(jsonPath("$.content[0].postedAt").value(postedAt.withZoneSameInstant(UTC).format(ISO_ZONED_DATE_TIME)))
                    .andExpect(jsonPath("$.totalItems").value(1))
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }

        @Test
        public void givenTwoPostsWithSameTime_shouldReturnLastInsertedFirst() throws Exception {
            ZonedDateTime now = ZonedDateTime.now();
            var post1 = TimelinePostDTO.builder()
                    .postSlug("postSlug1")
                    .userSlug("userSlug")
                    .username("username")
                    .profileName("profileName")
                    .content("content")
                    .postedAt(now)
                    .build();

            var post2 = post1.toBuilder()
                    .postSlug("postSlug2")
                    .postedAt(now)
                    .build();

            timelineService.addPostToPublicTimeline(post1);
            timelineService.addPostToPublicTimeline(post2);

            mockMvc.perform(get(READ_TIMELINE_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].postSlug").value(post2.getPostSlug()))
                    .andExpect(jsonPath("$.content[1].postSlug").value(post1.getPostSlug()))
                    .andExpect(jsonPath("$.totalItems").value(2))
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }

        @Test
        public void givenTwelvePostsAndFilterByScore_shouldReturnFilteredContent() throws Exception {
            var latestPost = TimelinePostDTO.builder()
                    .postSlug("postSlug0")
                    .userSlug("userSlug")
                    .username("username")
                    .profileName("profileName")
                    .content("content")
                    .postedAt(ZonedDateTime.now())
                    .build();

            List<TimelinePostDTO> allPosts = new ArrayList<>();
            allPosts.add(latestPost);
            timelineService.addPostToPublicTimeline(latestPost);
            for (int i = 1; i <= 11; i++) {
                var olderPost = latestPost.toBuilder().postSlug(latestPost.getPostSlug() + i).postedAt(ZonedDateTime.now().minusDays(i)).build();
                timelineService.addPostToPublicTimeline(olderPost);
                allPosts.add(olderPost);
            }

            //get the first page to retrieve the nextScore
            var mvcResult = mockMvc.perform(get(READ_TIMELINE_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(10))
                    .andExpect(jsonPath("$.nextScore").isNotEmpty())
                    .andReturn();
            PublicTimelineResponseDTO response = JsonUtils.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
            });
            String nextScore = response.getNextScore();

            var oldestPost = allPosts.get(11);
            var postAfterOldestPost = allPosts.get(10);
            mockMvc.perform(get(READ_TIMELINE_PATH).queryParam("nextScore", String.valueOf(nextScore)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].postSlug").value(postAfterOldestPost.getPostSlug()))
                    .andExpect(jsonPath("$.content[1].postSlug").value(oldestPost.getPostSlug()))
                    .andExpect(jsonPath("$.totalItems").value("12"))
                    .andExpect(jsonPath("$.nextScore").isEmpty());
        }
    }
}

