package minhdo.swe.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Long createdBy;
    private long memberCount;
    private LocalDateTime createdAt;
}
