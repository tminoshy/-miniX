package minhdo.swe.project.mapper;

import minhdo.swe.project.dto.SubredditDetailResponse;
import minhdo.swe.project.dto.SubredditResponse;
import minhdo.swe.project.entity.Subreddit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubredditMapper {

    @Mapping(source = "subreddit.id", target = "id")
    @Mapping(source = "subreddit.name", target = "name")
    @Mapping(source = "subreddit.description", target = "description")
    @Mapping(source = "subreddit.iconUrl", target = "iconUrl")
    @Mapping(source = "subreddit.createdBy.id", target = "createdBy")
    @Mapping(source = "memberCount", target = "memberCount")
    @Mapping(source = "subreddit.createdAt", target = "createdAt")
    SubredditResponse toSubredditResponse(Subreddit subreddit, long memberCount);

    @Mapping(source = "subreddit.id", target = "id")
    @Mapping(source = "subreddit.name", target = "name")
    @Mapping(source = "subreddit.description", target = "description")
    @Mapping(source = "subreddit.iconUrl", target = "iconUrl")
    @Mapping(source = "subreddit.createdBy", target = "createdBy")
    @Mapping(source = "memberCount", target = "memberCount")
    @Mapping(source = "isMember", target = "member")
    @Mapping(source = "subreddit.createdAt", target = "createdAt")
    SubredditDetailResponse toDetailResponse(Subreddit subreddit, long memberCount, boolean isMember);

    default SubredditDetailResponse.CreatorInfo mapCreator(minhdo.swe.project.entity.User user) {
        if (user == null)
            return null;
        return new SubredditDetailResponse.CreatorInfo(user.getId(), user.getUsername());
    }
}
