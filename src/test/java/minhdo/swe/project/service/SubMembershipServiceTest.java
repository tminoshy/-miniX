package minhdo.swe.project.service;

import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMembership;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.SubMemberRepository;
import minhdo.swe.project.repository.SubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubMembershipServiceTest {

    @Mock private SubRepository subRepository;
    @Mock private SubMemberRepository subMemberRepository;

    @InjectMocks private SubMembershipService subMembershipService;

    private User user;
    private User otherUser;
    private Sub sub;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("creator").build();
        otherUser = User.builder().id(2L).username("other").build();
        sub = Sub.builder().id(1L).name("testsub").description("A test sub")
                .iconUrl("icon.png").createdBy(user).createdAt(LocalDateTime.now()).build();
    }

    // ─── join ────────────────────────────────────────────────────────

    @Test
    void join_success() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.existsByUserAndSub(otherUser, sub)).thenReturn(false);

        subMembershipService.join("testsub", otherUser);

        verify(subMemberRepository).save(argThat(member ->
                member.getRole() == SubMembership.Role.Member &&
                member.getUser().equals(otherUser)));
    }

    @Test
    void join_alreadyMember_throwsException() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.existsByUserAndSub(user, sub)).thenReturn(true);

        assertThatThrownBy(() -> subMembershipService.join("testsub", user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Already a member");
    }

    @Test
    void join_subNotFound_throwsException() {
        when(subRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subMembershipService.join("unknown", user))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ─── leave ───────────────────────────────────────────────────────

    @Test
    void leave_success() {
        SubMembership member = SubMembership.builder()
                .id(1L).user(otherUser).sub(sub).role(SubMembership.Role.Member).build();

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.findByUserAndSub(otherUser, sub)).thenReturn(Optional.of(member));

        subMembershipService.leave("testsub", otherUser);

        verify(subMemberRepository).delete(member);
    }

    @Test
    void leave_moderator_throwsException() {
        SubMembership moderator = SubMembership.builder()
                .id(1L).user(user).sub(sub).role(SubMembership.Role.Moderator).build();

        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.findByUserAndSub(user, sub)).thenReturn(Optional.of(moderator));

        assertThatThrownBy(() -> subMembershipService.leave("testsub", user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Moderators cannot leave");
    }

    @Test
    void leave_notMember_throwsException() {
        when(subRepository.findByName("testsub")).thenReturn(Optional.of(sub));
        when(subMemberRepository.findByUserAndSub(otherUser, sub)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subMembershipService.leave("testsub", otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not a member");
    }

    @Test
    void leave_subNotFound_throwsException() {
        when(subRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subMembershipService.leave("unknown", user))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
