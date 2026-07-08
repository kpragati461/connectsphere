package com.connectsphere.service;

import com.connectsphere.model.Like;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.LikeRepository;
import com.connectsphere.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public boolean toggleLike(Long postId, String username) {
        User user = userService.findByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Like> existing = likeRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // unliked
        } else {
            likeRepository.save(Like.builder().user(user).post(post).build());
            return true; // liked
        }
    }

    public long getLikeCount(Post post) {
        return likeRepository.countByPost(post);
    }

    public boolean isLikedByUser(Post post, String username) {
        try {
            User user = userService.findByUsername(username);
            return likeRepository.existsByUserAndPost(user, post);
        } catch (Exception e) {
            return false;
        }
    }
}
