package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserInfoDetail user;

    @Data
    @AllArgsConstructor
    @Builder
    public static class UserInfoDetail {
        private Long id;
        private String username;
        private String email;
        private LocalDateTime createdAt;
    }
}
