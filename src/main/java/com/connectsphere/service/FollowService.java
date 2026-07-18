package com.connectsphere.service;

import com.connectsphere.model.Follow;
import com.connectsphere.model.User;
import com.connectsphere.model.NotificationType;
import com.connectsphere.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public boolean toggleFollow(String followerUsername, String followingUsername) {
        if (followerUsername.equals(followingUsername))
        throw new RuntimeException("You cannot follow yourself");

        User follower = userService.findByUsername(followerUsername);
        User following = userService.findByUsername(followingUsername);

        Optional<Follow> existing = followRepository
            .findByFollowerAndFollowing(follower, following);

        if (existing.isPresent()) {
           followRepository.delete(existing.get());
           return false; // unfollowed
        } else {
        followRepository.save(Follow.builder()
                .follower(follower)
                .following(following)
                .build());

        notificationService.createNotification(
                followingUsername,
                followerUsername,
                NotificationType.FOLLOW,
                null);

        return true; // followed
        }
    }

    public long getFollowerCount(User user) {
        return followRepository.countByFollowing(user);
    }

    public long getFollowingCount(User user) {
        return followRepository.countByFollower(user);
    }

    public boolean isFollowing(String followerUsername, String followingUsername) {
        try {
            User follower = userService.findByUsername(followerUsername);
            User following = userService.findByUsername(followingUsername);
            return followRepository.existsByFollowerAndFollowing(follower, following);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getFollowingUsernames(String username) {
        User user = userService.findByUsername(username);
        return followRepository.findByFollower(user)
                .stream()
                .map(f -> f.getFollowing().getUsername())
                .collect(Collectors.toList());
    }
}