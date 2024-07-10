package com.company.SocialNetwork.timeline;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimelineController {

    public static final String READ_TIMELINE_PATH = "/timeline";

    private final TimelineService timelineService;

    @GetMapping(READ_TIMELINE_PATH)
    public ResponseEntity<?> readPublicTimeline(@RequestParam(required = false) Double nextScore) {
        return ResponseEntity.ok(timelineService.readPublicTimeline(nextScore));
    }
}
