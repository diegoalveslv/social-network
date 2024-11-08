package com.company.SocialNetwork.timeline;

import com.company.SocialNetwork.shared.CountNewPostsResponseDTO;
import com.company.SocialNetwork.shared.PublicTimelineResponseDTO;
import com.company.SocialNetwork.utils.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Timeline")
public class TimelineController {

    public static final String READ_TIMELINE_PATH = "/timeline";
    public static final String COUNT_NEW_POSTS_PATH = "/timeline/count";

    private final TimelineService timelineService;

    @Operation(summary = "Read public timeline", operationId = "readPublicTimeline")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusCodes.OK, description = "OK",
                    content = @Content(schema = @Schema(implementation = PublicTimelineResponseDTO.class))),
    })
    @GetMapping(READ_TIMELINE_PATH)
    public ResponseEntity<?> readPublicTimeline(@Parameter(description = "Next score to start reading timeline from") @RequestParam(required = false) String nextScore) {
        return ResponseEntity.ok(timelineService.readPublicTimeline(nextScore));
    }

    @Operation(summary = "Count new posts", operationId = "countNewPosts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "CREATED")
    })
    @GetMapping(COUNT_NEW_POSTS_PATH)
    public ResponseEntity<?> countNewPosts(@Parameter(description = "Next score to start counting new posts") @RequestParam String nextScore) {

        Long commentPostSlug = timelineService.countNewPosts(nextScore);

        return ResponseEntity.ok(new CountNewPostsResponseDTO(String.valueOf(commentPostSlug), null));
    }
}
