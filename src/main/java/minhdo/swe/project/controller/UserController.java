package minhdo.swe.project.controller;

import minhdo.swe.project.dto.UserProfileResponse;
import minhdo.swe.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> body) {
        String avatarUrl = body.get("avatar_url");
        return ResponseEntity.ok(userService.updateAvatar(principal.getUsername(), avatarUrl));
    }
}
