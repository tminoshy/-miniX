package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreateCommentRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMember;
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

        return commentRepository.findByPostAndIsDeletedFalseOrderByCreatedAtDesc(post, pageable)
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

    @Transactional
    public CommentResponse createReply(User currentUser, Long postId, Long parentCommentId, CreateCommentRequest createCommentRequest) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(postId)));

        Sub currentSub = post.getSub();

        if (!subMemberRepository.existsByUserAndSub(currentUser, currentSub)) {
            throw new NotAllowedException("You must join sub before commenting");
        }

        Comment parentComment = commentRepository.findByIdAndPostAndIsDeletedFalse(parentCommentId, post)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", String.valueOf(parentCommentId)));

        Comment reply = commentMapper.toEntity(createCommentRequest);

        reply.setPost(post);
        reply.setUser(currentUser);
        reply.setParent(parentComment);

        return commentMapper.toCommentResponse(commentRepository.save(reply));
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long postId, Long commentId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(postId)));

        Comment comment = commentRepository.findByIdAndPostAndIsDeletedFalse(commentId, post)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", String.valueOf(commentId)));

        return commentMapper.toCommentResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, User currentUser, minhdo.swe.project.dto.request.UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", String.valueOf(commentId)));

        if (!comment.getUser().equals(currentUser)) {
            throw new NotAllowedException("You can only edit your own comments");
        }

        comment.setBody(request.getBody());
        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", String.valueOf(commentId)));

        Sub sub = comment.getPost().getSub();
        boolean isOwner = comment.getUser().equals(user);
        boolean isModerator = subMemberRepository.existsByUserAndSubAndRole(user, sub, SubMember.Role.Moderator);

        if (!isOwner && !isModerator) {
            throw new NotAllowedException("You are not allowed to delete this comment");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }
}
