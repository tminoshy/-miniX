package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.RefreshTokenRequest;
import minhdo.swe.project.dto.response.AuthResponse;
import minhdo.swe.project.entity.RefreshToken;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // Rotate: delete old, issue new
        refreshTokenRepository.delete(refreshToken);

        User user = refreshToken.getUser();
        return authService.buildAuthResponse(user);
    }

    RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                        .token(UUID.randomUUID().toString())
                                .user(user)
                                        .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                                                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
