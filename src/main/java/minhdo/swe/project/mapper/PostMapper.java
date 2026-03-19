package minhdo.swe.project.mapper;

import minhdo.swe.project.dto.request.CreatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SubMapper.class})
public interface PostMapper {

    @Mapping(source = "user", target = "userInfo")
    @Mapping(source = "sub", target = "subInfo")
    PostResponse toPostResponse(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "sub", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Post toEntity(CreatePostRequest request);
}