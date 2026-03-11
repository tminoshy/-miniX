package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityUtils securityUtils;

    @PostMapping("/subs/{name}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable("name") String subName,
            @Valid @RequestBody CreatePostRequest request) {
        User user = securityUtils.getCurrentUser();
        PostResponse response = postService.createPost(user, subName, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
//        return ResponseEntity.ok(postService.getPost(id));
//    }
//
    @GetMapping("/subs/{name}/posts")
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PathVariable("name") String subName,
            Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPostsBySub(subName, pageable));
    }
//
//    @GetMapping("/sub/{subId}")
//    public ResponseEntity<List<PostResponse>> getPostsBySub(@PathVariable Long subId) {
//        return ResponseEntity.ok(postService.getPostsBySub(subId));
//    }
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<PostResponse>> getPostsByUser(@PathVariable Long userId) {
//        return ResponseEntity.ok(postService.getPostsByUser(userId));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePost(
//            @AuthenticationPrincipal UserDetails principal,
//            @PathVariable Long id) {
//        postService.deletePost(principal.getUsername(), id);
//        return ResponseEntity.noContent().build();
//    }
}
