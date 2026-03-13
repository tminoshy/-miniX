package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreateCommentRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.NotAllowedException;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.CommentMapper;
import minhdo.swe.project.repository.CommentRepository;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SubMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final SubMemberRepository subMemberRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(postId)));

        return commentRepository.findByPostOrderByCreatedAtDesc(post, pageable)
                .map(commentMapper::toCommentResponse);
    }

    public CommentResponse createComment(User currentUser, Long postId, CreateCommentRequest createCommentRequest) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", String.valueOf(postId)));

        Sub currentSub = post.getSub();

        if (!subMemberRepository.existsByUserAndSub(currentUser, currentSub)) {
            throw new NotAllowedException("You must join sub before commenting");
        }

        Comment comment = commentMapper.toEntity(createCommentRequest);

        comment.setPost(post);
        comment.setUser(currentUser);

        return commentMapper.toCommentResponse(commentRepository.save(comment));

    }
}
