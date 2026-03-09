package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.*;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.SubredditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subs")
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<SubredditResponse> createSubreddit(
            @Valid @RequestBody CreateSubredditRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        SubredditResponse response = subredditService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<SubredditDetailResponse> getSubreddit(@PathVariable String name) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(subredditService.getByName(name, currentUser));
    }

    @PutMapping("/{name}")
    public ResponseEntity<SubredditDetailResponse> updateSubreddit(
            @PathVariable String name,
            @Valid @RequestBody UpdateSubredditRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        SubredditDetailResponse response = subredditService.update(name, currentUser, request);
        return ResponseEntity.ok(response);
    }
}
