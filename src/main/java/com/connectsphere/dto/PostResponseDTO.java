package com.connectsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDTO {
    private Long id;
    private String content;
    private String mediaUrl;
    private String username;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime feedExpiresAt;
}