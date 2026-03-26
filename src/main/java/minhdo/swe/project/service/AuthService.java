package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.LoginRequest;
import minhdo.swe.project.dto.request.RegisterRequest;
import minhdo.swe.project.dto.response.AuthResponse;
import minhdo.swe.project.entity.Role;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.UserRepository;
import minhdo.swe.project.security.JwtUtil;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;


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

        return tokenService.buildAuthResponse(
                userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return tokenService.buildAuthResponse(user);
    }

    @Transactional
    public void logout(String authHeader) {
        // Optionally: could parse token to find user and delete their refresh tokens
    }

}
