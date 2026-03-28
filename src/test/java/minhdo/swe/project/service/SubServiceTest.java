package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.CreateSubRequest;
import minhdo.swe.project.dto.request.UpdateSubRequest;
import minhdo.swe.project.dto.response.*;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMembership;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.mapper.SubMapper;
import minhdo.swe.project.mapper.UserMapper;
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
    @Mock private SubMemberRepository subMemberRepository;
    @Mock private SubMapper subMapper;
    @Mock private UserMapper userMapper;

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
        // Pass the same subMemberRepository mock for the single `subMemberRepository` constructor arg
        subService = new SubService(subRepository, subMemberRepository, subMapper, userMapper);

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
        when(subMemberRepository.save(any(SubMembership.class))).thenReturn(new SubMembership());
        when(subMapper.toSubResponse(any(Sub.class), eq(1L))).thenReturn(subResponse);

        SubResponse result = subService.create(user, request);

        assertThat(result.getName()).isEqualTo("testsub");
        verify(subRepository).save(any(Sub.class));
        verify(subMemberRepository).save(argThat(member ->
                member.getRole() == SubMembership.Role.Moderator));
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
        when(subMemberRepository.countBySub(sub)).thenReturn(10L);
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);
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
        when(subMemberRepository.existsByUserAndSubAndRole(user, sub, SubMembership.Role.Moderator)).thenReturn(true);
        when(subRepository.save(any(Sub.class))).thenReturn(sub);
        when(subMemberRepository.countBySub(sub)).thenReturn(10L);
        when(subMapper.toDetailResponse(any(Sub.class), eq(10L), eq(true))).thenReturn(subDetailResponse);

        SubDetailResponse result = subService.update("testsub", user, request);

        assertThat(result).isNotNull();
        verify(subRepository).save(sub);
    }

    @Test
    void update_notModerator_throwsException() {
        UpdateSubRequest request = new UpdateSubRequest("desc", "icon.png");

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.existsByUserAndSubAndRole(otherUser, sub, SubMembership.Role.Moderator)).thenReturn(false);

        assertThatThrownBy(() -> subService.update("testsub", otherUser, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only moderators can update this sub");
    }

    // ─── showAllMember ───────────────────────────────────────────────

    @Test
    void showAllMember_success() {
        Pageable pageable = PageRequest.of(0, 20);
        SubMembership member = SubMembership.builder().user(user).sub(sub).role(SubMembership.Role.Member).build();
        Page<SubMembership> memberPage = new PageImpl<>(List.of(member));

        UserProfileResponse profileResponse = new UserProfileResponse(1L, "creator", null, LocalDateTime.now());

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.findAllBySub(sub, pageable)).thenReturn(memberPage);
        when(userMapper.toProfileResponse(user)).thenReturn(profileResponse);

        Page<UserProfileResponse> result = subService.showAllMember("testsub", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

}
