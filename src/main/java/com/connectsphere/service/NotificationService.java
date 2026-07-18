package com.connectsphere.service;

import com.connectsphere.dto.NotificationResponseDTO;
import com.connectsphere.model.Notification;
import com.connectsphere.model.NotificationType;
import com.connectsphere.model.User;
import com.connectsphere.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public void createNotification(String recipientUsername,
                                    String actorUsername,
                                    NotificationType type,
                                    Long postId) {
        // don't notify yourself
        if (recipientUsername.equals(actorUsername)) return;

        User recipient = userService.findByUsername(recipientUsername);
        User actor = userService.findByUsername(actorUsername);

        Notification notification = Notification.builder()
                .user(recipient)
                .actor(actor)
                .type(type)
                .postId(postId)
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationResponseDTO> getNotifications(String username) {
        User user = userService.findByUsername(username);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        User user = userService.findByUsername(username);
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAllAsRead(String username) {
        User user = userService.findByUsername(username);
        notificationRepository.markAllAsRead(user);
    }

    private NotificationResponseDTO mapToDTO(Notification n) {
        String message = switch (n.getType()) {
            case LIKE    -> n.getActor().getUsername() + " liked your post";
            case COMMENT -> n.getActor().getUsername() + " commented on your post";
            case FOLLOW  -> n.getActor().getUsername() + " started following you";
        };

        return new NotificationResponseDTO(
                n.getId(),
                n.getActor().getUsername(),
                n.getType(),
                n.getPostId(),
                n.isRead(),
                n.getCreatedAt(),
                message
        );
    }
}