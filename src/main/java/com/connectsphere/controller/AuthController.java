package com.connectsphere.controller;

import com.connectsphere.dto.AuthResponse;
import com.connectsphere.dto.LoginRequest;
import com.connectsphere.dto.RegisterRequest;
import com.connectsphere.model.User;
import com.connectsphere.security.JwtUtil;
import com.connectsphere.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req) {

        User user = userService.registerUser(req);
        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(
                new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUsername(), req.getPassword()));

        User user = userService.findByUsername(req.getUsername());
        String token = jwtUtil.generateToken(req.getUsername());
        return ResponseEntity.ok(
                new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }
}