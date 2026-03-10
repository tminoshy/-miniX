package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.*;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.SubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/subs")
@RequiredArgsConstructor
public class SubController {

    private final SubService subService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<SubResponse> createSub(
            @Valid @RequestBody CreateSubRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        SubResponse response = subService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<SubDetailResponse> getSub(@PathVariable String name) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(subService.getByName(name, currentUser));
    }

    @PutMapping("/{name}")
    public ResponseEntity<SubDetailResponse> updateSub(
            @PathVariable String name,
            @Valid @RequestBody UpdateSubRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        SubDetailResponse response = subService.update(name, currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{name}/join")
    public ResponseEntity<Map<String, String>> joinSub(@PathVariable String name) {
        User currentUser = securityUtils.getCurrentUser();
        subService.join(name, currentUser);
        return ResponseEntity.ok(Map.of("message", "Joined successfully"));
    }

    @DeleteMapping("/{name}/leave")
    public ResponseEntity<Map<String, String>> leaveSub(@PathVariable String name) {
        User currentUser = securityUtils.getCurrentUser();
        subService.leave(name, currentUser);
        return ResponseEntity.ok(Map.of("message", "Left successfully"));
    }
}
