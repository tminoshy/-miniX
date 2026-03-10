package minhdo.swe.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateSubRequest {
    @NotBlank
    @Size(max = 21)
    private String name;
    private String description;
    private String iconUrl;
}
