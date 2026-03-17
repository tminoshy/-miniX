package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.response.SavedItemResponse;
import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.SavedItem;
import minhdo.swe.project.entity.SavedItemType;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.CommentMapper;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.CommentRepository;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SavedItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedItemService {

    private final SavedItemRepository savedItemRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public void savePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId.toString()));

        Optional<SavedItem> existing = savedItemRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            savedItemRepository.delete(existing.get());
        } else {
            SavedItem savedItem = SavedItem.builder()
                    .user(user)
                    .type(SavedItemType.POST)
                    .post(post)
                    .build();
            savedItemRepository.save(savedItem);
        }
    }

    public void saveComment(User user, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId.toString()));

        Optional<SavedItem> existing = savedItemRepository.findByUserAndComment(user, comment);
        if (existing.isPresent()) {
            savedItemRepository.delete(existing.get());
        } else {
            SavedItem savedItem = SavedItem.builder()
                    .user(user)
                    .type(SavedItemType.COMMENT)
                    .comment(comment)
                    .build();
            savedItemRepository.save(savedItem);
        }
    }

    public void unsavePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId.toString()));
        
        Optional<SavedItem> existing = savedItemRepository.findByUserAndPost(user, post);
        existing.ifPresent(savedItemRepository::delete);
    }

    public void unsaveComment(User user, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId.toString()));

        Optional<SavedItem> existing = savedItemRepository.findByUserAndComment(user, comment);
        existing.ifPresent(savedItemRepository::delete);
    }

    @Transactional(readOnly = true)
    public Page<SavedItemResponse> getSavedItems(User user, SavedItemType type, Pageable pageable) {
        Page<SavedItem> savedItems;
        if (type != null) {
            savedItems = savedItemRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type, pageable);
        } else {
            savedItems = savedItemRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        return savedItems.map(item -> {
            SavedItemResponse response = SavedItemResponse.builder()
                    .id(item.getId())
                    .type(item.getType())
                    .savedAt(item.getCreatedAt())
                    .build();

            if (item.getType() == SavedItemType.POST && item.getPost() != null) {
                response.setPost(postMapper.toPostResponse(item.getPost()));
            } else if (item.getType() == SavedItemType.COMMENT && item.getComment() != null) {
                response.setComment(commentMapper.toCommentResponse(item.getComment()));
            }

            return response;
        });
    }
}
