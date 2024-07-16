package com.company.SocialNetwork.post;

import com.company.SocialNetwork.timeline.TimelinePostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "slug", target = "postSlug")
    @Mapping(source = "userAccount.slug", target = "userSlug")
    @Mapping(source = "userAccount.username", target = "username")
    @Mapping(source = "userAccount.profileName", target = "profileName")
    @Mapping(source = "createdAt", target = "postedAt")
    TimelinePostDTO toTimelinePostDTO(Post post);
}
