package minhdo.swe.project.controller;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.ChangePasswordRequest;
import minhdo.swe.project.dto.response.UserProfileDetailResponse;
import minhdo.swe.project.dto.response.UserProfileResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDetailResponse> getMe() {
        User currentUser = securityUtils.getCurrentUser();
        UserProfileDetailResponse me = userService.getMe(currentUser);
        return ResponseEntity.ok(me);
    }

    @PostMapping("/me/password")
    public ResponseEntity<UserProfileDetailResponse> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        User currentUser = securityUtils.getCurrentUser();
        var me = userService.changePassword(currentUser, changePasswordRequest);
        return ResponseEntity.ok(me);
    }
}
