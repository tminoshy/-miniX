package minhdo.swe.project.service;

import minhdo.swe.project.dto.AuthResponse;
import minhdo.swe.project.dto.LoginRequest;
import minhdo.swe.project.dto.RefreshTokenRequest;
import minhdo.swe.project.dto.RegisterRequest;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.UserRepository;
import minhdo.swe.project.security.JwtUtil;
import minhdo.swe.project.security.TokenBlacklist;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       TokenBlacklist tokenBlacklist,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setKarma(0);

        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.isValid(token) || !jwtUtil.isRefreshToken(token)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklist.isBlacklisted(jti)) {
            throw new IllegalArgumentException("Refresh token has been invalidated");
        }

        // Rotate: blacklist old refresh token
        tokenBlacklist.blacklist(jti);

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        if (jwtUtil.isValid(token)) {
            tokenBlacklist.blacklist(jwtUtil.extractJti(token));
        }
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getKarma(),
                user.getCreatedAt()
        );
        return new AuthResponse(accessToken, refreshToken, userInfo);
    }
}
