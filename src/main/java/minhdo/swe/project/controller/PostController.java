package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.UpdatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request) {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(postService.updatePost(id, user, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id, securityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    //
//    @GetMapping("/sub/{subId}")
//    public ResponseEntity<List<PostResponse>> getPostsBySub(@PathVariable Long subId) {
//        return ResponseEntity.ok(getPostsBySub(subId));
//    }
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<PostResponse>> getPostsByUser(@PathVariable Long userId) {
//        return ResponseEntity.ok(getPostsByUser(userId));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePost(
//            @AuthenticationPrincipal UserDetails principal,
//            @PathVariable Long id) {
//        deletePost(principal.getUsername(), id);
//        return ResponseEntity.noContent().build();
//    }
}
