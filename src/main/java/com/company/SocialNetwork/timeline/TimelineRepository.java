package com.company.SocialNetwork.timeline;

import java.util.Optional;

public interface TimelineRepository {

    LatestPostsDTO getLatestPosts(Optional<Double> nextScore);

    String getTotalItemsString();

    void addPostToPublicTimeline(TimelinePostDTO timelinePost);
}
