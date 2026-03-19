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
    private UserInfo userInfo;
    private SubInfo subInfo;
    private Integer score;
    private LocalDateTime createdAt;
}
