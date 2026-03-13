package minhdo.swe.project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private String body;
    private UserInfo userInfo;
    private Long postId;
    private Long parentId;
    private Integer score;
    private LocalDateTime createdAt;
}
