package minhdo.swe.project.service;

import minhdo.swe.project.dto.request.ChangePasswordRequest;
import minhdo.swe.project.dto.response.UserProfileDetailResponse;
import minhdo.swe.project.dto.response.UserProfileResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.mapper.UserMapper;
import minhdo.swe.project.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return userMapper.toProfileResponse(user);
    }

    public UserProfileDetailResponse getMe(User user) {
        return userMapper.toProfileDetailResponse(user);
    }

    public UserProfileDetailResponse changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        String hashPassword = user.getPasswordHash();
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), hashPassword)) {
            throw new BadCredentialsException("Incorrect password");
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassowrd()));
        return userMapper.toProfileDetailResponse(userRepository.save(user));
    }
}
