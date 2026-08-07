package com.connectsphere.controller;

import com.connectsphere.service.BlockService;
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
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{username}/block")
    public ResponseEntity<?> toggleBlock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        try {
            boolean blocked = blockService.toggleBlock(
                    userDetails.getUsername(), username);
            return ResponseEntity.ok(Map.of("blocked", blocked));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{username}/block-status")
    public ResponseEntity<Map<String, Object>> getBlockStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        boolean blockedByMe = blockService.isBlocked(
                userDetails.getUsername(), username);
        boolean blockedByThem = blockService.isBlocked(
                username, userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "blockedByMe", blockedByMe,
                "blockedByThem", blockedByThem,
                "blocked", blockedByMe || blockedByThem
        ));
    }

    // NEW — lets a user see and manage who they've blocked, independent of
    // search/profile, both of which correctly hide blocked users from view.
    @GetMapping("/blocked")
    public ResponseEntity<List<String>> getBlockedUsers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                blockService.getBlockedUsernames(userDetails.getUsername()));
    }
}