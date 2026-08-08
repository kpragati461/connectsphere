package com.connectsphere.controller;

import com.connectsphere.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final CloudinaryService cloudinaryService;

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    // Post media — image or video, auto-detected.
    @PostMapping("/upload-post")
    public ResponseEntity<?> uploadPostMedia(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File too large (max 25MB)"));
        }
        try {
            String url = cloudinaryService.upload(file, "connectsphere/posts");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // Profile picture — images only.
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File too large (max 25MB)"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Avatar must be an image"));
        }
        try {
            String url = cloudinaryService.uploadImage(file, "connectsphere/avatars");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}