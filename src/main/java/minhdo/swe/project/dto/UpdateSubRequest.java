package minhdo.swe.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateSubRequest {
    private String description;
    private String iconUrl;
}
