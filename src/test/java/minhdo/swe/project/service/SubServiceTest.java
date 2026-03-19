package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.CreatePostRequest;
import minhdo.swe.project.dto.request.CreateSubRequest;
import minhdo.swe.project.dto.request.UpdateSubRequest;
import minhdo.swe.project.dto.response.*;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMember;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.mapper.SubMapper;
import minhdo.swe.project.mapper.UserMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SubMemberRepository;
import minhdo.swe.project.repository.SubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubServiceTest {

    @Mock private SubRepository subRepository;
    @Mock private SubMemberRepository memberRepository;
    @Mock private SubMapper subMapper;
    @Mock private UserMapper userMapper;
    @Mock private PostMapper postMapper;
    @Mock private PostRepository postRepository;

    // Constructed manually because SubService has two fields of the same type (SubMemberRepository).
    // @InjectMocks would create separate mocks for each, causing stubs to hit the wrong one.
    private SubService subService;

    private User user;
    private User otherUser;
    private Sub sub;
    private SubResponse subResponse;
    private SubDetailResponse subDetailResponse;

    @BeforeEach
    void setUp() {
        // Pass the same memberRepository mock for both `memberRepository` and `subMemberRepository` constructor args
        subService = new SubService(subRepository, memberRepository, subMapper, userMapper,
                memberRepository, postMapper, postRepository);

        user = User.builder().id(1L).username("creator").build();
        otherUser = User.builder().id(2L).username("other").build();
        sub = Sub.builder().id(1L).name("testsub").description("A test sub")
                .iconUrl("icon.png").createdBy(user).createdAt(LocalDateTime.now()).build();

        subResponse = new SubResponse(1L, "testsub", "A test sub", "icon.png", 1L, 1, LocalDateTime.now());

        UserInfo creatorInfo = new UserInfo();
        creatorInfo.setId(1L);
        creatorInfo.setUsername("creator");
        subDetailResponse = new SubDetailResponse(1L, "testsub", "A test sub", "icon.png",
                creatorInfo, 1, true, LocalDateTime.now());
    }

    // ─── create ──────────────────────────────────────────────────────

    @Test
    void create_success() {
        CreateSubRequest request = new CreateSubRequest("testsub", "A test sub", "icon.png");

        when(subRepository.existsByName("testsub")).thenReturn(false);
        when(subRepository.save(any(Sub.class))).thenReturn(sub);
        when(memberRepository.save(any(SubMember.class))).thenReturn(new SubMember());
        when(subMapper.toSubResponse(any(Sub.class), eq(1L))).thenReturn(subResponse);

        SubResponse result = subService.create(user, request);

        assertThat(result.getName()).isEqualTo("testsub");
        verify(subRepository).save(any(Sub.class));
        verify(memberRepository).save(argThat(member ->
                member.getRole() == SubMember.Role.Moderator));
    }

    @Test
    void create_duplicateName_throwsException() {
        CreateSubRequest request = new CreateSubRequest("testsub", "desc", "icon.png");

        when(subRepository.existsByName("testsub")).thenReturn(true);

        assertThatThrownBy(() -> subService.create(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sub name already taken");
    }

    // ─── getByName ───────────────────────────────────────────────────

    @Test
    void getByName_success() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.countBySub(sub)).thenReturn(10L);
        when(memberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(subMapper.toDetailResponse(sub, 10L, true)).thenReturn(subDetailResponse);

        SubDetailResponse result = subService.getByName("testsub", user);

        assertThat(result.getName()).isEqualTo("testsub");
        assertThat(result.isMember()).isTrue();
    }

    @Test
    void getByName_notFound_throwsException() {
        when(subRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subService.getByName("unknown", user))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ─── update ──────────────────────────────────────────────────────

    @Test
    void update_success() {
        UpdateSubRequest request = new UpdateSubRequest("Updated desc", "new-icon.png");

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.existsByUserAndSubAndRole(user, sub, SubMember.Role.Moderator)).thenReturn(true);
        when(subRepository.save(any(Sub.class))).thenReturn(sub);
        when(memberRepository.countBySub(sub)).thenReturn(10L);
        when(subMapper.toDetailResponse(any(Sub.class), eq(10L), eq(true))).thenReturn(subDetailResponse);

        SubDetailResponse result = subService.update("testsub", user, request);

        assertThat(result).isNotNull();
        verify(subRepository).save(sub);
    }

    @Test
    void update_notModerator_throwsException() {
        UpdateSubRequest request = new UpdateSubRequest("desc", "icon.png");

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.existsByUserAndSubAndRole(otherUser, sub, SubMember.Role.Moderator)).thenReturn(false);

        assertThatThrownBy(() -> subService.update("testsub", otherUser, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only moderators can update this sub");
    }

    // ─── join ────────────────────────────────────────────────────────

    @Test
    void join_success() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.existsByUserAndSub(otherUser, sub)).thenReturn(false);

        subService.join("testsub", otherUser);

        verify(memberRepository).save(argThat(member ->
                member.getRole() == SubMember.Role.Member &&
                member.getUser().equals(otherUser)));
    }

    @Test
    void join_alreadyMember_throwsException() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.existsByUserAndSub(user, sub)).thenReturn(true);

        assertThatThrownBy(() -> subService.join("testsub", user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Already a member");
    }

    // ─── leave ───────────────────────────────────────────────────────

    @Test
    void leave_success() {
        SubMember member = SubMember.builder()
                .id(1L).user(otherUser).sub(sub).role(SubMember.Role.Member).build();

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.findByUserAndSub(otherUser, sub)).thenReturn(Optional.of(member));

        subService.leave("testsub", otherUser);

        verify(memberRepository).delete(member);
    }

    @Test
    void leave_moderator_throwsException() {
        SubMember moderator = SubMember.builder()
                .id(1L).user(user).sub(sub).role(SubMember.Role.Moderator).build();

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.findByUserAndSub(user, sub)).thenReturn(Optional.of(moderator));

        assertThatThrownBy(() -> subService.leave("testsub", user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Moderators cannot leave");
    }

    @Test
    void leave_notMember_throwsException() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.findByUserAndSub(otherUser, sub)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subService.leave("testsub", otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not a member");
    }

    // ─── showAllMember ───────────────────────────────────────────────

    @Test
    void showAllMember_success() {
        Pageable pageable = PageRequest.of(0, 20);
        SubMember member = SubMember.builder().user(user).sub(sub).role(SubMember.Role.Member).build();
        Page<SubMember> memberPage = new PageImpl<>(List.of(member));

        UserProfileResponse profileResponse = new UserProfileResponse(1L, "creator", null, LocalDateTime.now());

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.findAllBySub(sub, pageable)).thenReturn(memberPage);
        when(userMapper.toProfileResponse(user)).thenReturn(profileResponse);

        Page<UserProfileResponse> result = subService.showAllMember("testsub", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ─── createPost ──────────────────────────────────────────────────

    @Test
    void createPost_success() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setBody("Post body");

        Post newPost = Post.builder().id(2L).title("New Post").body("Post body")
                .user(user).sub(sub).score(0).isDeleted(false).build();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("creator");
        SubInfo subInfo = new SubInfo(1L, "testsub");
        PostResponse response = new PostResponse(2L, "New Post", "Post body", userInfo, subInfo, 0, LocalDateTime.now());

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
        when(postMapper.toEntity(request)).thenReturn(newPost);
        when(postRepository.save(any(Post.class))).thenReturn(newPost);
        when(postMapper.toPostResponse(newPost)).thenReturn(response);

        PostResponse result = subService.createPost(user, "testsub", request);

        assertThat(result.getTitle()).isEqualTo("New Post");
        verify(postRepository).save(newPost);
    }

    @Test
    void createPost_notMember_throwsException() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(memberRepository.existsByUserAndSub(otherUser, sub)).thenReturn(false);

        assertThatThrownBy(() -> subService.createPost(otherUser, "testsub", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user is not in sub");
    }

    // ─── getPostsBySub ───────────────────────────────────────────────

    @Test
    void getPostsBySub_success() {
        Pageable pageable = PageRequest.of(0, 20);
        Post post = Post.builder().id(1L).title("T").sub(sub).user(user).score(0).isDeleted(false).build();
        Page<Post> postPage = new PageImpl<>(List.of(post));

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("creator");
        SubInfo subInfo = new SubInfo(1L, "testsub");
        PostResponse response = new PostResponse(1L, "T", null, userInfo, subInfo, 0, LocalDateTime.now());

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(postRepository.findBySubOrderByCreatedAtDesc(sub, pageable)).thenReturn(postPage);
        when(postMapper.toPostResponse(post)).thenReturn(response);

        Page<PostResponse> result = subService.getPostsBySub("testsub", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPostsBySub_subNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(subRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subService.getPostsBySub("unknown", pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
