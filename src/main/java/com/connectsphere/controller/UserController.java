package com.connectsphere.controller;

import com.connectsphere.dto.UpdateProfileRequest;
import com.connectsphere.dto.UserResponseDTO;
import com.connectsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me — get logged in user's profile
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getProfile(userDetails.getUsername()));
    }

    // GET /api/users/{username} — get any user's profile
    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    // PUT /api/users/me — update logged in user's profile
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(
                userService.updateProfile(userDetails.getUsername(), req));
    }
}