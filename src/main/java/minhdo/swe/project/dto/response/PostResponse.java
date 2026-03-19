package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse implements Serializable {
    private Long id;
    private String title;
    private String body;
    private UserInfo userInfo;
    private SubInfo subInfo;
    private Integer score;
    private LocalDateTime createdAt;
}
