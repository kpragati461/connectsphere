package com.connectsphere.dto;

import com.connectsphere.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String profilePhoto;
    private Role role;
    private LocalDateTime createdAt;
    private long followerCount;
    private long followingCount;
    private boolean followedByCurrentUser;
}