package com.connectsphere.controller;

import com.connectsphere.dto.CreatePostRequest;
import com.connectsphere.dto.PostResponseDTO;
import com.connectsphere.service.BlockService;
import com.connectsphere.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final BlockService blockService;

@PostMapping
public ResponseEntity<?> createPost(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody CreatePostRequest req) {
    try {
        return ResponseEntity.ok(
                postService.createPost(userDetails.getUsername(), req));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

    @GetMapping("/feed")
    public ResponseEntity<List<PostResponseDTO>> getFeed(
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.getFeed(userDetails.getUsername()));
    }

   @GetMapping("/user/{username}")
public ResponseEntity<?> getUserPosts(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable String username) {

    // check block in both directions
    if (blockService.isBlocked(userDetails.getUsername(), username) ||
        blockService.isBlocked(username, userDetails.getUsername())) {
        return ResponseEntity.status(403)
                .body(Map.of("error", "You cannot view this user's posts"));
    }

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