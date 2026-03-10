package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import minhdo.swe.project.dto.CreatePostRequest;
import minhdo.swe.project.dto.PostResponse;
import minhdo.swe.project.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.createPost(principal.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/sub/{subId}")
    public ResponseEntity<List<PostResponse>> getPostsBySub(@PathVariable Long subId) {
        return ResponseEntity.ok(postService.getPostsBySub(subId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getPostsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        postService.deletePost(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
