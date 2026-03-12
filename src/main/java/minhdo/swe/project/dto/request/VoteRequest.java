package minhdo.swe.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {

    @NotNull
    private Short value; // +1 or -1
}
