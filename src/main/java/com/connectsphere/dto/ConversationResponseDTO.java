package com.connectsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponseDTO {
    private Long id;
    private String otherUsername;
    private String otherProfilePhoto;
    private String lastMessage;
    private LocalDateTime createdAt;
    private long unreadCount;
}
