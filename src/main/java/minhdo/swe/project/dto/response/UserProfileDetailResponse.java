package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserProfileDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
