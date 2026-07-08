package com.connectsphere.controller;

import com.connectsphere.dto.CreatePostRequest;
import com.connectsphere.dto.PostResponseDTO;
import com.connectsphere.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePostRequest req) {
        return ResponseEntity.ok(
                postService.createPost(userDetails.getUsername(), req));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostResponseDTO>> getFeed(
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.getFeed(userDetails.getUsername()));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(
            @PathVariable String username) {
        return ResponseEntity.ok(postService.getUserPosts(username));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}