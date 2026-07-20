package com.connectsphere.service;

import com.connectsphere.dto.UserResponseDTO;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.CommentRepository;
import com.connectsphere.repository.PostRepository;
import com.connectsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FollowService followService;

    public Map<String, Long> getStats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "totalPosts", postRepository.count(),
                "totalComments", commentRepository.count()
        );
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getBio(),
                        user.getProfilePhoto(),
                        user.getRole(),
                        user.getCreatedAt(),
                        followService.getFollowerCount(user),
                        followService.getFollowingCount(user),
                        false
                ))
                .collect(Collectors.toList());
    }

    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(true);
        userRepository.save(user);
    }

    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(false);
        userRepository.save(user);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        commentRepository.deleteAll(
                commentRepository.findByPostOrderByCreatedAtAsc(post));
        postRepository.delete(post);
    }

    public void deleteComment(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.deleteById(commentId);
    }
}
