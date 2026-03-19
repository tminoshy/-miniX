package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse implements Serializable {

    private Long id;
    private String body;
    private UserInfo userInfo;
    private Long postId;
    private Long parentId;
    private Integer score;
    private LocalDateTime createdAt;
}
