package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse implements Serializable {
    private Long id;
    private String username;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
