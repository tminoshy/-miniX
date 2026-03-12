package minhdo.swe.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import minhdo.swe.project.entity.VoteType;

@Data
public class VoteRequest {

    @NotNull
    private VoteType voteType;
}
