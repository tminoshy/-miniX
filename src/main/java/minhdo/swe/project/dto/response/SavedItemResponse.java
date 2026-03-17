package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import minhdo.swe.project.entity.SavedItemType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedItemResponse {
    private Long id;
    private SavedItemType type;
    private PostResponse post;
    private CommentResponse comment;
    private LocalDateTime savedAt;
}
