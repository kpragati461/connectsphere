package com.connectsphere.controller;

import com.connectsphere.dto.UpdateProfileRequest;
import com.connectsphere.dto.UserResponseDTO;
import com.connectsphere.service.FollowService;
import com.connectsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
}