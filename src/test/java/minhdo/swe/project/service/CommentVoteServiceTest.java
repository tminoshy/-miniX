package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.VoteRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.dto.response.UserInfo;
import minhdo.swe.project.entity.*;
import minhdo.swe.project.exception.NotAllowedException;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.CommentMapper;
import minhdo.swe.project.repository.CommentRepository;
import minhdo.swe.project.repository.CommentVoteRepository;
import minhdo.swe.project.repository.SubMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentVoteServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private CommentVoteRepository commentVoteRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private SubMemberRepository subMemberRepository;

    @InjectMocks private CommentVoteService commentVoteService;

    private User user;
    private Sub sub;
    private Post post;
    private Comment comment;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("voter").build();
        sub = Sub.builder().id(1L).name("sub").build();
        post = Post.builder().id(1L).sub(sub).build();
        comment = Comment.builder()
                .id(1L).body("Comment").user(user).post(post)
                .score(10).isDeleted(false).createdAt(LocalDateTime.now())
                .build();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("voter");
        commentResponse = CommentResponse.builder()
                .id(1L).body("Comment").score(10).userInfo(userInfo)
                .build();
    }

    // ─── vote ────────────────────────────────────────────────────────

    @Test
    void vote_newUpvote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(commentVoteRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.empty());
        when(commentVoteRepository.save(any(CommentVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        commentVoteService.vote(1L, user, request);

        assertThat(comment.getScore()).isEqualTo(11);
        verify(commentVoteRepository).save(any(CommentVote.class));
    }

    @Test
    void vote_newDownvote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.DOWNVOTE);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(commentVoteRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.empty());
        when(commentVoteRepository.save(any(CommentVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        commentVoteService.vote(1L, user, request);

        assertThat(comment.getScore()).isEqualTo(9);
    }

    @Test
    void vote_changingVote_upvoteToDownvote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.DOWNVOTE);

        CommentVote existingVote = CommentVote.builder()
                .id(1L).comment(comment).user(user).voteType(VoteType.UPVOTE).build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(commentVoteRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.of(existingVote));
        when(commentVoteRepository.save(existingVote)).thenReturn(existingVote);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        commentVoteService.vote(1L, user, request);

        // score was 10, remove upvote (-1), add downvote (-1) = 8
        assertThat(comment.getScore()).isEqualTo(8);
        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.DOWNVOTE);
    }

    @Test
    void vote_sameVote_removesVote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        CommentVote existingVote = CommentVote.builder()
                .id(1L).comment(comment).user(user).voteType(VoteType.UPVOTE).build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(commentVoteRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.of(existingVote));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

        commentVoteService.vote(1L, user, request);

        // Was 10, removing upvote: 10 - 1 = 9
        assertThat(comment.getScore()).isEqualTo(9);
        verify(commentVoteRepository).delete(existingVote);
    }

    @Test
    void vote_notMember_throwsException() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(false);

        assertThatThrownBy(() -> commentVoteService.vote(1L, user, request))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    void vote_commentNotFound_throwsException() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentVoteService.vote(99L, user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── removeVote ──────────────────────────────────────────────────

    @Test
    void removeVote_success() {
        CommentVote existingVote = CommentVote.builder()
                .id(1L).comment(comment).user(user).voteType(VoteType.UPVOTE).build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentVoteRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.of(existingVote));

        commentVoteService.removeVote(1L, user);

        // Was 10, removing upvote: 10 - 1 = 9
        assertThat(comment.getScore()).isEqualTo(9);
        verify(commentVoteRepository).delete(existingVote);
        verify(commentRepository).save(comment);
    }

    @Test
    void removeVote_noExistingVote_throwsException() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentVoteRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentVoteService.removeVote(1L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You have not voted on this comment");
    }
}
