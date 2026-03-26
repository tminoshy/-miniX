package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.LoginRequest;
import minhdo.swe.project.dto.request.RegisterRequest;
import minhdo.swe.project.dto.response.AuthResponse;
import minhdo.swe.project.entity.Role;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User testUser;
    private AuthResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        expectedResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(AuthResponse.UserInfoDetail.builder()
                        .id(1L).username("testuser").email("test@example.com").build())
                .build();
    }

    // ─── Register ────────────────────────────────────────────────────

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenService.buildAuthResponse(testUser)).thenReturn(expectedResponse);

        AuthResponse result = authService.register(request);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
        verify(tokenService).buildAuthResponse(testUser);
    }

    @Test
    void register_duplicateUsername_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
        verify(userRepository, never()).save(any());
    }

    // ─── Login ───────────────────────────────────────────────────────

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tokenService.buildAuthResponse(testUser)).thenReturn(expectedResponse);

        AuthResponse result = authService.login(request);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        verify(tokenService).buildAuthResponse(testUser);
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password123");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }
}