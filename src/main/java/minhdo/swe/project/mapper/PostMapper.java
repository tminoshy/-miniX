package minhdo.swe.project.mapper;

import minhdo.swe.project.dto.request.CreatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "sub.id", target = "subId")
    @Mapping(source = "user.username", target = "username")
    PostResponse toPostResponse(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "sub", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Post toEntity(CreatePostRequest request);
}