package com.connectsphere.controller;

import com.connectsphere.dto.ChangePasswordRequest;
import com.connectsphere.dto.UpdateProfileRequest;
import com.connectsphere.dto.UserResponseDTO;
import com.connectsphere.service.FollowService;
import com.connectsphere.service.UserService;
import com.connectsphere.dto.ChangePasswordRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getProfile(userDetails.getUsername(),
                        userDetails.getUsername()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        return ResponseEntity.ok(
                userService.getProfile(username, userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(
                userService.updateProfile(userDetails.getUsername(), req));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        boolean followed = followService.toggleFollow(
                userDetails.getUsername(), username);
        return ResponseEntity.ok(Map.of("followed", followed));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String query) {
        return ResponseEntity.ok(
                userService.searchUsers(query, userDetails.getUsername()));
    }

    // NEW — powers the "Share to..." picker (Instagram-style send-to-DM sheet).
    @GetMapping("/following")
    public ResponseEntity<List<UserResponseDTO>> getFollowing(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getFollowing(userDetails.getUsername()));
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<List<UserResponseDTO>> getFollowers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        return ResponseEntity.ok(
                userService.getFollowers(username, userDetails.getUsername()));
    }

    @GetMapping("/{username}/following-list")
    public ResponseEntity<List<UserResponseDTO>> getFollowingList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        return ResponseEntity.ok(
                userService.getFollowingOf(username, userDetails.getUsername()));
    }
    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePasswordRequest req) {
    try {
        userService.changePassword(userDetails.getUsername(), req);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
@PostMapping("/me/verify-password")
public ResponseEntity<?> verifyPassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody Map<String, String> body) {
    try {
        userService.verifyCurrentPassword(userDetails.getUsername(), body.get("currentPassword"));
        return ResponseEntity.ok(Map.of("valid", true));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
}