package minhdo.swe.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private CreatorInfo createdBy;
    private long memberCount;
    private boolean member;
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    public static class CreatorInfo {
        private Long id;
        private String username;
    }
}
