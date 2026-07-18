package com.connectsphere.dto;

import com.connectsphere.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private String actorUsername;
    private NotificationType type;
    private Long postId;
    private boolean read;
    private LocalDateTime createdAt;
    private String message;
}