package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.CreateCommentRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.dto.response.UserInfo;
import minhdo.swe.project.entity.*;
import minhdo.swe.project.exception.NotAllowedException;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.CommentMapper;
import minhdo.swe.project.repository.CommentRepository;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SubMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private SubMemberRepository subMemberRepository;

    @InjectMocks private CommentService commentService;

    private User user;
    private Sub sub;
    private Post post;
    private Comment comment;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        sub = Sub.builder().id(1L).name("testsub").build();
        post = Post.builder()
                .id(1L).title("Test Post").body("Body")
                .user(user).sub(sub).score(0).isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        comment = Comment.builder()
                .id(1L).body("Test comment").post(post).user(user).score(0)
                .createdAt(LocalDateTime.now())
                .build();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        commentResponse = CommentResponse.builder()
                .id(1L).body("Test comment").userInfo(userInfo)
                .postId(1L).score(0).createdAt(LocalDateTime.now())
                .build();
    }

    // ─── getCommentsByPost ───────────────────────────────────────────

    @Test
    void getCommentsByPost_success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostAndIsDeletedFalseOrderByCreatedAtDesc(post, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        Page<CommentResponse> result = commentService.getCommentsByPost(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBody()).isEqualTo("Test comment");
    }

    @Test
    void getCommentsByPost_postNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentsByPost(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCommentsByPost_postDeleted_throwsException() {
        Pageable pageable = PageRequest.of(0, 20);
        post.setIsDeleted(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.getCommentsByPost(1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── createComment ───────────────────────────────────────────────

    @Test
    void createComment_success() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("New comment");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(commentMapper.toEntity(request)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = commentService.createComment(user, 1L, request);

        assertThat(result).isNotNull();
        verify(commentRepository).save(comment);
    }

    @Test
    void createComment_notMember_throwsException() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("New comment");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(false);

        assertThatThrownBy(() -> commentService.createComment(user, 1L, request))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("You must join sub before commenting");
    }

    @Test
    void createComment_postNotFound_throwsException() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("New comment");

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(user, 99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── createReply ─────────────────────────────────────────────────

    @Test
    void createReply_success() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Reply");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(commentRepository.findByIdAndPostAndIsDeletedFalse(1L, post)).thenReturn(Optional.of(comment));
        
        Comment replyEntity = Comment.builder().body("Reply").build();
        when(commentMapper.toEntity(request)).thenReturn(replyEntity);
        
        Comment savedReply = Comment.builder().id(2L).body("Reply").parent(comment).build();
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReply);
        
        CommentResponse replyResponse = CommentResponse.builder().id(2L).body("Reply").parentId(1L).build();
        when(commentMapper.toCommentResponse(savedReply)).thenReturn(replyResponse);

        CommentResponse result = commentService.createReply(user, 1L, 1L, request);

        assertThat(result.getParentId()).isEqualTo(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    // ─── getCommentById ──────────────────────────────────────────────

    @Test
    void getCommentById_success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndPostAndIsDeletedFalse(1L, post)).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = commentService.getCommentById(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    // ─── updateComment ───────────────────────────────────────────────

    @Test
    void updateComment_success() {
        minhdo.swe.project.dto.request.UpdateCommentRequest request = new minhdo.swe.project.dto.request.UpdateCommentRequest("Updated Body");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        
        CommentResponse updatedResponse = CommentResponse.builder().id(1L).body("Updated Body").build();
        when(commentMapper.toCommentResponse(comment)).thenReturn(updatedResponse);

        CommentResponse result = commentService.updateComment(1L, user, request);

        assertThat(comment.getBody()).isEqualTo("Updated Body");
        assertThat(result.getBody()).isEqualTo("Updated Body");
        verify(commentRepository).save(comment);
    }

    @Test
    void updateComment_notOwner_throwsException() {
        minhdo.swe.project.dto.request.UpdateCommentRequest request = new minhdo.swe.project.dto.request.UpdateCommentRequest("Updated Body");
        User otherUser = User.builder().id(2L).username("other").build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(1L, otherUser, request))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("You can only edit your own comments");
    }

    @Test
    void updateComment_commentDeleted_throwsException() {
        minhdo.swe.project.dto.request.UpdateCommentRequest request = new minhdo.swe.project.dto.request.UpdateCommentRequest("Updated Body");
        comment.setIsDeleted(true);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(1L, user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteComment ───────────────────────────────────────────────

    @Test
    void deleteComment_byOwner_success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, user);

        assertThat(comment.getIsDeleted()).isTrue();
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteComment_byModerator_success() {
        User moderator = User.builder().id(2L).username("mod").build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSubAndRole(moderator, sub, SubMember.Role.Moderator)).thenReturn(true);

        commentService.deleteComment(1L, moderator);

        assertThat(comment.getIsDeleted()).isTrue();
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteComment_notAuthorized_throwsException() {
        User otherUser = User.builder().id(3L).username("other").build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSubAndRole(otherUser, sub, SubMember.Role.Moderator)).thenReturn(false);

        assertThatThrownBy(() -> commentService.deleteComment(1L, otherUser))
                .isInstanceOf(NotAllowedException.class);
    }
}
