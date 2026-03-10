package minhdo.swe.project.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
