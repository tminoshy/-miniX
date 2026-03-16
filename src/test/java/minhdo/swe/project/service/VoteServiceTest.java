package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.VoteRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.dto.response.UserInfo;
import minhdo.swe.project.entity.*;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.PostVoteRepository;
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
class VoteServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private PostVoteRepository postVoteRepository;
    @Mock private PostMapper postMapper;

    @InjectMocks private VoteService voteService;

    private User user;
    private Post post;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("voter").build();
        post = Post.builder()
                .id(1L).title("Post").body("Body").user(user)
                .sub(Sub.builder().id(1L).name("sub").build())
                .score(10).isDeleted(false).createdAt(LocalDateTime.now())
                .build();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("voter");
        postResponse = new PostResponse(1L, "Post", "Body", userInfo, 1L, 10, LocalDateTime.now());
    }

    // ─── vote ────────────────────────────────────────────────────────

    @Test
    void vote_newUpvote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postVoteRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());
        when(postVoteRepository.save(any(PostVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        PostResponse result = voteService.vote(1L, user, request);

        assertThat(post.getScore()).isEqualTo(11);
        verify(postVoteRepository).save(any(PostVote.class));
    }

    @Test
    void vote_newDownvote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.DOWNVOTE);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postVoteRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());
        when(postVoteRepository.save(any(PostVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        voteService.vote(1L, user, request);

        assertThat(post.getScore()).isEqualTo(9);
    }

    @Test
    void vote_changingVote_upvoteToDownvote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.DOWNVOTE);

        PostVote existingVote = PostVote.builder()
                .id(1L).post(post).user(user).voteType(VoteType.UPVOTE).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postVoteRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(existingVote));
        when(postVoteRepository.save(existingVote)).thenReturn(existingVote);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        voteService.vote(1L, user, request);

        // score was 10, remove upvote (-1), add downvote (-1) = 8
        assertThat(post.getScore()).isEqualTo(8);
        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.DOWNVOTE);
    }

    @Test
    void vote_sameVote_removesVote() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        PostVote existingVote = PostVote.builder()
                .id(1L).post(post).user(user).voteType(VoteType.UPVOTE).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postVoteRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(existingVote));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        voteService.vote(1L, user, request);

        // Was 10, removing upvote: 10 - 1 = 9
        assertThat(post.getScore()).isEqualTo(9);
        verify(postVoteRepository).delete(existingVote);
    }

    @Test
    void vote_postNotFound_throwsException() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voteService.vote(99L, user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void vote_postDeleted_throwsException() {
        VoteRequest request = new VoteRequest();
        request.setVoteType(VoteType.UPVOTE);

        post.setIsDeleted(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> voteService.vote(1L, user, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── removeVote ──────────────────────────────────────────────────

    @Test
    void removeVote_success() {
        PostVote existingVote = PostVote.builder()
                .id(1L).post(post).user(user).voteType(VoteType.UPVOTE).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postVoteRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(existingVote));

        voteService.removeVote(1L, user);

        // Was 10, removing upvote: 10 - 1 = 9
        assertThat(post.getScore()).isEqualTo(9);
        verify(postVoteRepository).delete(existingVote);
        verify(postRepository).save(post);
    }

    @Test
    void removeVote_noExistingVote_throwsException() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postVoteRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voteService.removeVote(1L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You have not voted on this post");
    }
}
