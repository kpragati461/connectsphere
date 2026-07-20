package com.connectsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    private Long id;
    private Long conversationId;
    private String senderUsername;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
}