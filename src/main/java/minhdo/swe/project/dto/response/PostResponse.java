package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String body;
    private Long userId;
    private String username;
    private Long subId;
    private Integer score;
    private LocalDateTime createdAt;
}
