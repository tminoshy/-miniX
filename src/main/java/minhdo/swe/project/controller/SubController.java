package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.*;
import minhdo.swe.project.dto.response.*;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.PostService;
import minhdo.swe.project.service.SubMembershipService;
import minhdo.swe.project.service.SubService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/subs")
@RequiredArgsConstructor
public class SubController {

    private final SubService subService;
    private final SubMembershipService subMembershipService;
    private final SecurityUtils securityUtils;
    private final PostService postService;

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
    public ResponseEntity<String> joinSub(@PathVariable String name) {
        User currentUser = securityUtils.getCurrentUser();
        subMembershipService.join(name, currentUser);
        return ResponseEntity.ok("Join successfully");
    }

    @DeleteMapping("/{name}/leave")
    public ResponseEntity<String> leaveSub(@PathVariable String name) {
        User currentUser = securityUtils.getCurrentUser();
        subMembershipService.leave(name, currentUser);
        return ResponseEntity.ok("Leave successfully");
    }

    @GetMapping("/{name}/members")
    public ResponseEntity<Page<UserProfileResponse>> getAllUser(@PathVariable("name") String subName, Pageable pageable) {

        var response = subService.showAllMember(subName, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{name}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable("name") String subName,
            @Valid @RequestBody CreatePostRequest request) {
        User user = securityUtils.getCurrentUser();
        PostResponse response = postService.createPost(user, subName, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //    @GetMapping("/{id}")
//    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
//        return ResponseEntity.ok(getPost(id));
//    }
//
    @GetMapping("/{name}/posts")
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PathVariable("name") String subName,
            Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsBySub(subName, pageable));
    }
}
