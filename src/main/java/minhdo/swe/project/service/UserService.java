package minhdo.swe.project.service;

import minhdo.swe.project.dto.UserProfileResponse;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return toProfileResponse(user);
    }

    public UserProfileResponse updateAvatar(String username, String avatarUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        user.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getKarma(),
                user.getCreatedAt()
        );
    }
}
