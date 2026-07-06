package com.connectsphere.service;

import com.connectsphere.dto.CreatePostRequest;
import com.connectsphere.dto.PostResponseDTO;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public PostResponseDTO createPost(String username, CreatePostRequest req) {
        User user = userService.findByUsername(username);
        Post post = Post.builder()
                .content(req.getContent())
                .mediaUrl(req.getMediaUrl())
                .user(user)
                .build();
        return mapToDTO(postRepository.save(post));
    }

    public List<PostResponseDTO> getFeed() {
        return postRepository
                .findByFeedExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PostResponseDTO> getUserPosts(String username) {
        User user = userService.findByUsername(username);
        return postRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getUsername().equals(username))
            throw new RuntimeException("You can only delete your own posts");
        postRepository.delete(post);
    }

    private PostResponseDTO mapToDTO(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getUser().getUsername(),
                post.getUser().getProfilePhoto(),
                post.getCreatedAt(),
                post.getFeedExpiresAt()
        );
    }
}