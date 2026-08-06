package com.connectsphere.controller;

import com.connectsphere.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{username}/block")
    public ResponseEntity<Map<String, Object>> toggleBlock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        boolean blocked = blockService.toggleBlock(
                userDetails.getUsername(), username);
        return ResponseEntity.ok(Map.of("blocked", blocked));
    }

    @GetMapping("/{username}/block-status")
    public ResponseEntity<Map<String, Object>> getBlockStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
        boolean blocked = blockService.isBlocked(
                userDetails.getUsername(), username);
        return ResponseEntity.ok(Map.of("blocked", blocked));
    }
}