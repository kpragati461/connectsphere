package com.connectsphere.controller;

import com.connectsphere.dto.PostResponseDTO;
import com.connectsphere.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId) {
        boolean saved = bookmarkService.toggleBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getSavedPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                bookmarkService.getSavedPosts(userDetails.getUsername()));
    }
}
