package minhdo.swe.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotNull
    private String type; // "TEXT", "LINK", "IMAGE"

    private String body;

    @Size(max = 2000)
    private String url;

    @Size(max = 500)
    private String imageUrl;

    @NotNull
    private Long subId;
}
