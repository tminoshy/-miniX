package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.LoginRequest;
import minhdo.swe.project.dto.request.RefreshTokenRequest;
import minhdo.swe.project.dto.request.RegisterRequest;
import minhdo.swe.project.dto.response.AuthResponse;
import minhdo.swe.project.entity.RefreshToken;
import minhdo.swe.project.entity.Role;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.RefreshTokenRepository;
import minhdo.swe.project.repository.UserRepository;
import minhdo.swe.project.security.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpirationMs;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                        .username(request.getUsername())
                                .email(request.getEmail())
                                        .passwordHash(passwordEncoder.encode(request.getPassword()))
                                                .role(Role.USER)
                                                        .build();

        return buildAuthResponse(
                userRepository.save(user)
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return buildAuthResponse(user);
    }

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
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String authHeader) {
        // Optionally: could parse token to find user and delete their refresh tokens
    }

    private AuthResponse buildAuthResponse(User user) {

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshTokenStr = createRefreshToken(user).getToken();

        AuthResponse.UserInfoDetail userInfo = AuthResponse.UserInfoDetail.builder()
                .id(user.getId())
                    .username(user.getUsername())
                        .email(user.getEmail())
                            .createdAt(user.getCreatedAt())
                                .build();

        return new AuthResponse(accessToken, refreshTokenStr, userInfo);
    }

    private RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                        .token(UUID.randomUUID().toString())
                                .user(user)
                                        .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                                                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
