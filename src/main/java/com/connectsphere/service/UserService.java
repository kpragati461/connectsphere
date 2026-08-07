package com.connectsphere.service;

import com.connectsphere.dto.RegisterRequest;
import com.connectsphere.dto.UpdateProfileRequest;
import com.connectsphere.dto.UserResponseDTO;
import com.connectsphere.model.Role;
import com.connectsphere.model.User;
import com.connectsphere.repository.UserRepository;

import java.util.stream.Collectors;
import java.util.List;

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
    @Lazy
    private final BlockService blockService;

   public UserService(UserRepository userRepository,
                   PasswordEncoder passwordEncoder,
                   @Lazy FollowService followService,
                   @Lazy BlockService blockService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.followService = followService;
    this.blockService = blockService;
}

    @Override
public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found: " + username));

    if (user.isBanned()) {
        throw new UsernameNotFoundException(
                "Your account has been banned");
    }

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
                    followService.isFollowing(currentUsername, user.getUsername()),
            user.isBanned()
    );
}
    public List<UserResponseDTO> searchUsers(String query, String currentUsername) {
    List<String> blocked = blockService.getBlockedUsernames(currentUsername);
    // NEW — also hide people who have blocked ME. Previously you could still
    // find and view someone who'd blocked you, as long as you hadn't blocked
    // them back.
    List<String> blockedBy = blockService.getBlockedByUsernames(currentUsername);
    return userRepository.findByUsernameContainingIgnoreCase(query)
            .stream()
            .filter(user -> !blocked.contains(user.getUsername()))
            .filter(user -> !blockedBy.contains(user.getUsername()))
            .filter(user -> !user.getUsername().equals(currentUsername))
            .map(user -> mapToDTO(user, currentUsername))
            .collect(Collectors.toList());
}
    // profiles of everyone `username` follows, for the share-to-DM picker.
    public List<UserResponseDTO> getFollowing(String username) {
        // NEW — exclude blocked relationships in both directions. Previously
        // a blocked user could still appear here, letting a block be bypassed
        // entirely by sharing a post straight to their DMs.
        List<String> blocked = blockService.getBlockedUsernames(username);
        List<String> blockedBy = blockService.getBlockedByUsernames(username);
        return followService.getFollowingUsers(username)
                .stream()
                .filter(u -> !blocked.contains(u.getUsername()))
                .filter(u -> !blockedBy.contains(u.getUsername()))
                .map(u -> mapToDTO(u, username))
                .collect(Collectors.toList());
    }
}
