package minhdo.swe.project.mapper;

import minhdo.swe.project.dto.request.CreateCommentRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {

    Comment toEntity(CreateCommentRequest createCommentRequest);

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "user", target = "userInfo")
    CommentResponse toCommentResponse(Comment comment);
}
