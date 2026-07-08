package com.connectsphere.service;

import com.connectsphere.dto.CommentResponseDTO;
import com.connectsphere.dto.CreateCommentRequest;
import com.connectsphere.model.Comment;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.CommentRepository;
import com.connectsphere.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public CommentResponseDTO addComment(Long postId, String username,
                                         CreateCommentRequest req) {
        User user = userService.findByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .content(req.getContent())
                .user(user)
                .post(post)
                .build();

        return mapToDTO(commentRepository.save(comment));
    }

    public List<CommentResponseDTO> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getUser().getUsername().equals(username))
            throw new RuntimeException("You can only delete your own comments");
        commentRepository.delete(comment);
    }

    public long getCommentCount(Post post) {
        return commentRepository.countByPost(post);
    }

    private CommentResponseDTO mapToDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getCreatedAt()
        );
    }
}
