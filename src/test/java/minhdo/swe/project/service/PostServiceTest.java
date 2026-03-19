package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.UpdatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.dto.response.SubInfo;
import minhdo.swe.project.dto.response.UserInfo;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.UserRepository;
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
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostMapper postMapper;

    @InjectMocks private PostService postService;

    private User owner;
    private User otherUser;
    private Sub sub;
    private Post post;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("owner").build();
        otherUser = User.builder().id(2L).username("other").build();
        sub = Sub.builder().id(1L).name("testsub").build();

        post = Post.builder()
                .id(1L)
                .title("Test Title")
                .body("Test Body")
                .user(owner)
                .sub(sub)
                .score(0)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("owner");
        SubInfo subInfo = SubInfo.builder().id(1L).name("sub").build();

        postResponse = new PostResponse(1L, "Test Title", "Test Body", userInfo, subInfo, 0, LocalDateTime.now());
    }

    // ─── getPostById ─────────────────────────────────────────────────

    @Test
    void getPostById_success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        PostResponse result = postService.getPostById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Title");
    }

    @Test
    void getPostById_notFound_throwsException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPostById_deleted_throwsException() {
        post.setIsDeleted(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.getPostById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── updatePost ──────────────────────────────────────────────────

    @Test
    void updatePost_success() {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setBody("Updated Body");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toPostResponse(any(Post.class))).thenReturn(postResponse);

        PostResponse result = postService.updatePost(1L, owner, request);

        assertThat(result).isNotNull();
        verify(postRepository).save(post);
    }

    @Test
    void updatePost_notOwner_throwsException() {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setBody("Updated Body");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, otherUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you must be owner to modify the post");
    }

    @Test
    void updatePost_notFound_throwsException() {
        UpdatePostRequest request = new UpdatePostRequest();
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(99L, owner, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deletePost ──────────────────────────────────────────────────

    @Test
    void deletePost_success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, owner);

        assertThat(post.getIsDeleted()).isTrue();
        verify(postRepository).save(post);
    }

    @Test
    void deletePost_notOwner_throwsException() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(1L, otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can only delete your own posts");
    }

    // ─── getPostsByUser ──────────────────────────────────────────────

    @Test
    void getPostsByUser_success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(owner, pageable))
                .thenReturn(postPage);
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        Page<PostResponse> result = postService.getPostsByUser(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPostsByUser_userNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostsByUser(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
