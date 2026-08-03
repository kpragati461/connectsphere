package com.connectsphere.service;

import com.connectsphere.dto.PostResponseDTO;
import com.connectsphere.model.Bookmark;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.BookmarkRepository;
import com.connectsphere.repository.CommentRepository;
import com.connectsphere.repository.LikeRepository;
import com.connectsphere.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public boolean toggleBookmark(Long postId, String username) {
        User user = userService.findByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Bookmark> existing = bookmarkRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        } else {
            bookmarkRepository.save(Bookmark.builder()
                    .user(user)
                    .post(post)
                    .build());
            return true;
        }
    }

    public List<PostResponseDTO> getSavedPosts(String username) {
        User user = userService.findByUsername(username);
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(b -> mapToDTO(b.getPost(), username))
                .collect(Collectors.toList());
    }

    public boolean isBookmarked(Post post, String username) {
        try {
            User user = userService.findByUsername(username);
            return bookmarkRepository.existsByUserAndPost(user, post);
        } catch (Exception e) {
            return false;
        }
    }

    private PostResponseDTO mapToDTO(Post post, String username) {
        boolean liked = false;
        boolean bookmarked = false;
        try {
            User user = userService.findByUsername(username);
            liked = likeRepository.existsByUserAndPost(user, post);
            bookmarked = bookmarkRepository.existsByUserAndPost(user, post);
        } catch (Exception e) {
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
