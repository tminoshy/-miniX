package minhdo.swe.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreateCommentRequest;
import minhdo.swe.project.dto.request.UpdatePostRequest;
import minhdo.swe.project.dto.request.VoteRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.CommentService;
import minhdo.swe.project.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityUtils securityUtils;
    private final CommentService commentService;

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



    @PostMapping("/{id}/vote")
    public ResponseEntity<PostResponse> vote(
            @PathVariable Long id,
            @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(postService.vote(id, securityUtils.getCurrentUser(), request));
    }

    @DeleteMapping("/{id}/vote")
    public ResponseEntity<Void> removeVote(@PathVariable Long id) {
        postService.removeVote(id, securityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(@PathVariable("id") Long postId, Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, pageable));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable("id") Long postId, CreateCommentRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        CommentResponse response = commentService.createComment(currentUser, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
