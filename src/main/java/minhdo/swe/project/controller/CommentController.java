package minhdo.swe.project.controller;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreateCommentRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(@PathVariable("id") Long postId, Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, pageable));
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable("id") Long postId, @RequestBody CreateCommentRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        CommentResponse response = commentService.createComment(currentUser, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/posts/{id}/comments/{commentId}")
    public ResponseEntity<CommentResponse> createReply(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CreateCommentRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        CommentResponse response = commentService.createReply(currentUser, postId, commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/posts/{id}/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(postId, commentId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long commentId) {
        commentService.deleteComment(commentId, securityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable("id") Long commentId,
            @jakarta.validation.Valid @RequestBody minhdo.swe.project.dto.request.UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, securityUtils.getCurrentUser(), request));
    }
}
