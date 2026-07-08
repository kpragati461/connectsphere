package com.connectsphere.service;

import com.connectsphere.dto.RegisterRequest;
import com.connectsphere.dto.UpdateProfileRequest;
import com.connectsphere.dto.UserResponseDTO;
import com.connectsphere.model.Role;
import com.connectsphere.model.User;
import com.connectsphere.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final FollowService followService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Manual constructor (replaces @RequiredArgsConstructor)
    public UserService(@Lazy FollowService followService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.followService = followService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public User registerUser(RegisterRequest req) {

        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    // Updated
    public UserResponseDTO getProfile(String username, String currentUsername) {
        User user = findByUsername(username);
        return mapToDTO(user, currentUsername);
    }

    // Updated
    public UserResponseDTO updateProfile(String username, UpdateProfileRequest req) {
        User user = findByUsername(username);

        if (req.getBio() != null)
            user.setBio(req.getBio());

        if (req.getProfilePhoto() != null)
            user.setProfilePhoto(req.getProfilePhoto());

        userRepository.save(user);

        return mapToDTO(user, username);
    }

    // Updated
    private UserResponseDTO mapToDTO(User user, String currentUsername) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfilePhoto(),
                user.getRole(),
                user.getCreatedAt(),
                followService.getFollowerCount(user),
                followService.getFollowingCount(user),
                currentUsername != null &&
                        followService.isFollowing(currentUsername, user.getUsername())
        );
    }
}
