package minhdo.swe.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    String oldPassword;
    @NotBlank
    String newPassowrd;
}
