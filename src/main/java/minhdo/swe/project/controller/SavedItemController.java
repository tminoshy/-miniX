package minhdo.swe.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.response.SavedItemResponse;
import minhdo.swe.project.entity.SavedItemType;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.security.SecurityUtils;
import minhdo.swe.project.service.SavedItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved")
@RequiredArgsConstructor
@Tag(name = "Saved Items", description = "Endpoints for managing saved posts and comments")
public class SavedItemController {

    private final SavedItemService savedItemService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Save or unsave a post (toggle)")
    @PostMapping("/posts/{postId}")
    public ResponseEntity<Void> savePost(@PathVariable Long postId) {
        User user = securityUtils.getCurrentUser();
        savedItemService.savePost(user, postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Save or unsave a comment (toggle)")
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<Void> saveComment(@PathVariable Long commentId) {
        User user = securityUtils.getCurrentUser();
        savedItemService.saveComment(user, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Explicitly unsave a post")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> unsavePost(@PathVariable Long postId) {
        User user = securityUtils.getCurrentUser();
        savedItemService.unsavePost(user, postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Explicitly unsave a comment")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> unsaveComment(@PathVariable Long commentId) {
        User user = securityUtils.getCurrentUser();
        savedItemService.unsaveComment(user, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get list of saved items")
    @GetMapping
    public ResponseEntity<Page<SavedItemResponse>> getSavedItems(
            @RequestParam(required = false) SavedItemType type,
            @PageableDefault(size = 20) Pageable pageable) {
        User user = securityUtils.getCurrentUser();
        Page<SavedItemResponse> items = savedItemService.getSavedItems(user, type, pageable);
        return ResponseEntity.ok(items);
    }
}
