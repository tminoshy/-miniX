package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.ChangePasswordRequest;
import minhdo.swe.project.dto.response.UserProfileDetailResponse;
import minhdo.swe.project.dto.response.UserProfileResponse;
import minhdo.swe.project.entity.Role;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.mapper.UserMapper;
import minhdo.swe.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User user;
    private UserProfileResponse profileResponse;
    private UserProfileDetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .passwordHash("encoded_old").role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        profileResponse = new UserProfileResponse(1L, "testuser", null, LocalDateTime.now());
        detailResponse = new UserProfileDetailResponse(1L, "testuser", "test@example.com", null, LocalDateTime.now());
    }

    // ─── getProfile ──────────────────────────────────────────────────

    @Test
    void getProfile_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toProfileResponse(user)).thenReturn(profileResponse);

        UserProfileResponse result = userService.getProfile("testuser");

        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ─── getMe ───────────────────────────────────────────────────────

    @Test
    void getMe_success() {
        when(userMapper.toProfileDetailResponse(user)).thenReturn(detailResponse);

        UserProfileDetailResponse result = userService.getMe(user);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    // ─── changePassword ──────────────────────────────────────────────

    @Test
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old_password");
        request.setNewPassowrd("new_password");

        when(passwordEncoder.matches("old_password", "encoded_old")).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileDetailResponse(user)).thenReturn(detailResponse);

        UserProfileDetailResponse result = userService.changePassword(user, request);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("new_password");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong_password");
        request.setNewPassowrd("new_password");

        when(passwordEncoder.matches("wrong_password", "encoded_old")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(user, request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Incorrect password");
    }
}
