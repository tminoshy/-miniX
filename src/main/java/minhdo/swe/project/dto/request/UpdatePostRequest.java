package minhdo.swe.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    private String body;
}
