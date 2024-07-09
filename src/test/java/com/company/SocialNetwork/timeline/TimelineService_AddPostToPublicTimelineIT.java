package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.TestcontainersConfigurationPostgres;
import com.company.SocialNetwork.TestcontainersConfigurationRedis;
import com.company.SocialNetwork.utils.JsonUtils;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.company.SocialNetwork.timeline.TimelineService.PUBLIC_TIMELINE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest
@Import({TestcontainersConfigurationRedis.class, TestcontainersConfigurationPostgres.class})
//TODO postgres is not needed here but I added it to be able to load the context. Fix this
public class TimelineService_AddPostToPublicTimelineIT {

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ParameterizedTest
    @MethodSource("provideInvalidAddPostToPublicTimelineDTO")
    public void givenInvalidFieldInParams_thenThrowFieldValidationException(TimelinePostDTO request, String expectedMessage) {
        var ex = catchThrowableOfType(() -> timelineService.addPostToPublicTimeline(request), ConstraintViolationException.class);
        List<String> messages = ex.getConstraintViolations().stream().map(cv -> {
            String[] pathSplit = cv.getPropertyPath().toString().split("\\.");
            return "%s: %s".formatted(pathSplit[pathSplit.length - 1], cv.getMessage());
        }).toList();

        assertThat(messages).containsAnyOf(expectedMessage);
    }

    @Nested
    class WithRedisCache {

        @BeforeEach
        public void tearDown() {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }

        @Test
        public void givenValidRequest_() {
            var request = TimelinePostDTO.builder().postSlug("someslug").userSlug("someslug").profileName("profileName").username("username").content("content").postedAt(LocalDateTime.now()).build();
            timelineService.addPostToPublicTimeline(request);

            TimelinePostDTO resultPost = shouldAddPostToCache();
            assertThat(resultPost).isEqualTo(request);
        }

        @Test
        public void givenMultipleRequestsInTheSameInstant_() {
            var now = LocalDateTime.now();
            var request = TimelinePostDTO.builder().postSlug("someslug").userSlug("someslug").profileName("profileName").username("username").content("content").postedAt(now).build();
            var request2 = request.toBuilder().postSlug("anotherSlug").build();

            timelineService.addPostToPublicTimeline(request);
            timelineService.addPostToPublicTimeline(request2);

            Set<TimelinePostDTO> posts = shouldAddMultiplePostsToCache();
            assertThat(posts).hasSize(2);
            assertThat(posts).containsExactlyInAnyOrder(request, request2);
        }

        private Set<TimelinePostDTO> shouldAddMultiplePostsToCache() {
            Set<String> posts = redisTemplate.opsForZSet().range(PUBLIC_TIMELINE_KEY, 0, 10);
            assertThat(posts).isNotNull();

            return posts.stream().map(post -> JsonUtils.readValue(post, TimelinePostDTO.class)).collect(Collectors.toSet());
        }

        private TimelinePostDTO shouldAddPostToCache() {
            Set<String> posts = redisTemplate.opsForZSet().range(PUBLIC_TIMELINE_KEY, 0, 10);
            assertThat(posts).hasSize(1);

            List<TimelinePostDTO> postsList = posts.stream().map(post -> JsonUtils.readValue(post, TimelinePostDTO.class)).toList();
            return postsList.get(0);
        }
    }

    static Stream<Arguments> provideInvalidAddPostToPublicTimelineDTO() {
        var nullPostSlug = TimelinePostDTO.builder().postSlug(null).build();
        var blankPostSlug = TimelinePostDTO.builder().postSlug("").build();
        var emptySpacePostSlug = TimelinePostDTO.builder().postSlug(" ").build();
        var nullUserSlug = TimelinePostDTO.builder().userSlug(null).build();
        var blankUserSlug = TimelinePostDTO.builder().userSlug("").build();
        var emptySpaceUserSlug = TimelinePostDTO.builder().userSlug(" ").build();
        var nullUsername = TimelinePostDTO.builder().username(null).build();
        var blankUsername = TimelinePostDTO.builder().username("").build();
        var emptySpaceUsername = TimelinePostDTO.builder().username("  ").build();
        var nullProfileName = TimelinePostDTO.builder().profileName(null).build();
        var blankProfileName = TimelinePostDTO.builder().profileName("").build();
        var emptySpaceProfileName = TimelinePostDTO.builder().profileName("  ").build();
        var nullContent = TimelinePostDTO.builder().content(null).build();
        var blankContent = TimelinePostDTO.builder().content("").build();
        var emptySpaceContent = TimelinePostDTO.builder().content("   ").build();
        var nullPostedAt = TimelinePostDTO.builder().postedAt(null).build();

        return Stream.of(
                Arguments.of(nullPostSlug, "postSlug: must not be null")
                , Arguments.of(blankPostSlug, "postSlug: must not be blank")
                , Arguments.of(emptySpacePostSlug, "postSlug: must not be blank")
                , Arguments.of(nullUserSlug, "userSlug: must not be blank")
                , Arguments.of(blankUserSlug, "userSlug: must not be blank")
                , Arguments.of(emptySpaceUserSlug, "userSlug: must not be blank")
                , Arguments.of(nullUsername, "username: must not be null")
                , Arguments.of(blankUsername, "username: must not be blank")
                , Arguments.of(emptySpaceUsername, "username: must not be blank")
                , Arguments.of(nullProfileName, "profileName: must not be null")
                , Arguments.of(blankProfileName, "profileName: must not be blank")
                , Arguments.of(emptySpaceProfileName, "profileName: must not be blank")
                , Arguments.of(nullContent, "content: must not be null")
                , Arguments.of(blankContent, "content: must not be blank")
                , Arguments.of(emptySpaceContent, "content: must not be blank")
                , Arguments.of(nullPostedAt, "postedAt: must not be null")
        );
    }
}

