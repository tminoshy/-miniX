package minhdo.swe.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer karma;
    private LocalDateTime createdAt;
}
