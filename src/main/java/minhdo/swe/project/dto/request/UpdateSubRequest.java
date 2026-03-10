package minhdo.swe.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateSubRequest {
    private String description;
    private String iconUrl;
}
