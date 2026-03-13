package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.VoteRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VoteController {

    private final VoteService voteService;
    private final SecurityUtils securityUtils;

    @PostMapping("/posts/{id}/vote")
    public ResponseEntity<PostResponse> vote(
            @PathVariable Long id,
            @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(voteService.vote(id, securityUtils.getCurrentUser(), request));
    }

    @DeleteMapping("/posts/{id}/vote")
    public ResponseEntity<Void> removeVote(@PathVariable Long id) {
        voteService.removeVote(id, securityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
