package com.connectsphere.service;

import com.connectsphere.dto.CreatePostRequest;
import com.connectsphere.dto.PostResponseDTO;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.BookmarkRepository;
import com.connectsphere.repository.CommentRepository;
import com.connectsphere.repository.LikeRepository;
import com.connectsphere.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FollowService followService;
    private final BlockService blockService;

public PostResponseDTO createPost(String username, CreatePostRequest req) {
    boolean hasContent = req.getContent() != null && !req.getContent().isBlank();
    boolean hasMedia = req.getMediaUrl() != null && !req.getMediaUrl().isBlank();

    if (!hasContent && !hasMedia) {
        throw new IllegalArgumentException("Post must have text or media");
    }

    User user = userService.findByUsername(username);
    Post post = Post.builder()
            .content(req.getContent())
            .mediaUrl(req.getMediaUrl())
            .user(user)
            .build();
    return mapToDTO(postRepository.save(post), username);
}

public List<PostResponseDTO> getFeed(String username) {
    List<String> following = followService.getFollowingUsernames(username);
    following.add(username);

    List<String> blocked = blockService.getBlockedUsernames(username);
    // NEW — also exclude people who have blocked ME, not just people I blocked.
    // Without this, someone I blocked could still see my posts in their own
    // feed as long as they still followed me.
    List<String> blockedBy = blockService.getBlockedByUsernames(username);

    return postRepository
            .findByFeedExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime.now())
            .stream()
            .filter(post -> following.contains(post.getUser().getUsername()))
            .filter(post -> !blocked.contains(post.getUser().getUsername()))
            .filter(post -> !blockedBy.contains(post.getUser().getUsername()))
            .map(post -> mapToDTO(post, username))
            .collect(Collectors.toList());
}
public List<PostResponseDTO> getUserPosts(String username) {
    User user = userService.findByUsername(username);
    return postRepository.findByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(post -> mapToDTO(post, username))
            .collect(Collectors.toList());
}

    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getUsername().equals(username))
            throw new RuntimeException("You can only delete your own posts");
        postRepository.delete(post);
    }

    private PostResponseDTO mapToDTO(Post post, String username) {
        boolean liked = false;
        boolean bookmarked = false;
        if (username != null) {
            try {
                User user = userService.findByUsername(username);
                liked = likeRepository.existsByUserAndPost(user, post);
                bookmarked = bookmarkRepository.existsByUserAndPost(user, post);
            } catch (Exception e) {
                liked = false;
                bookmarked = false;
            }
        }
        return new PostResponseDTO(
                post.getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getUser().getUsername(),
                post.getUser().getProfilePhoto(),
                post.getCreatedAt(),
                post.getFeedExpiresAt(),
                likeRepository.countByPost(post),
                commentRepository.countByPost(post),
                liked,
                bookmarked
        );
    }
}