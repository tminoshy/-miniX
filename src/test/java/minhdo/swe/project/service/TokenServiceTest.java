package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.RefreshTokenRequest;
import minhdo.swe.project.dto.response.AuthResponse;
import minhdo.swe.project.entity.RefreshToken;
import minhdo.swe.project.entity.Role;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.RefreshTokenRepository;
import minhdo.swe.project.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "refreshExpirationMs", 604800000L);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── refresh ─────────────────────────────────────────────────────

    @Test
    void refresh_success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-token");

        RefreshToken storedToken = RefreshToken.builder()
                .id(1L)
                .token("valid-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(storedToken));
        when(jwtUtil.generateAccessToken("testuser")).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = tokenService.refresh(request);

        verify(refreshTokenRepository).delete(storedToken);
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void refresh_invalidToken_throwsException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void refresh_expiredToken_throwsException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .token("expired-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> tokenService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token has expired");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    // ─── buildAuthResponse ───────────────────────────────────────────

    @Test
    void buildAuthResponse_success() {
        when(jwtUtil.generateAccessToken("testuser")).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = tokenService.buildAuthResponse(testUser);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        verify(jwtUtil).generateAccessToken("testuser");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
}