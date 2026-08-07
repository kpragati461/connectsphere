package com.connectsphere.controller;

import com.connectsphere.dto.ConversationResponseDTO;
import com.connectsphere.dto.MessageResponseDTO;
import com.connectsphere.dto.SendMessageRequest;
import com.connectsphere.model.Conversation;
import com.connectsphere.model.NotificationType;
import com.connectsphere.service.BlockService;
import com.connectsphere.service.ChatService;
import com.connectsphere.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final BlockService blockService;

    // REST — get all conversations
    @GetMapping("/api/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                chatService.getConversations(userDetails.getUsername()));
    }

    @PostMapping("/api/conversations/{username}")
public ResponseEntity<Map<String, Long>> startConversation(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable String username) {

    if (blockService.isBlocked(userDetails.getUsername(), username) ||
        blockService.isBlocked(username, userDetails.getUsername())) {
        return ResponseEntity.status(403).build();
    }

    Conversation conv = chatService.getOrCreateConversation(
            userDetails.getUsername(), username);
    return ResponseEntity.ok(Map.of("conversationId", conv.getId()));
}

    // REST — get message history
    @GetMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(
                chatService.getMessages(conversationId, userDetails.getUsername()));
    }

   @PostMapping("/api/conversations/{conversationId}/messages")
public ResponseEntity<MessageResponseDTO> sendMessage(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long conversationId,
        @Valid @RequestBody SendMessageRequest req) {

    // check if either user has blocked the other
    String otherUsername = chatService.getOtherUsername(
            conversationId, userDetails.getUsername());

    if (blockService.isBlocked(userDetails.getUsername(), otherUsername) ||
        blockService.isBlocked(otherUsername, userDetails.getUsername())) {
        return ResponseEntity.status(403)
                .build();
    }

    MessageResponseDTO message = chatService.saveMessage(
            conversationId, userDetails.getUsername(), req);

    messagingTemplate.convertAndSend(
            "/topic/conversation." + conversationId, message);

    notificationService.createNotification(
            otherUsername,
            userDetails.getUsername(),
            NotificationType.MESSAGE,
            null);

    return ResponseEntity.ok(message);
}

    @PostMapping("/api/conversations/{conversationId}/share-post/{postId}")
    public ResponseEntity<?> sharePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @PathVariable Long postId) {

        // NEW — this endpoint had no block check at all, so sharing a post
        // was a way to message a blocked user even when startConversation
        // and sendMessage both correctly refused.
        String recipientUsername = chatService
                .getOtherUsername(conversationId, userDetails.getUsername());

        if (blockService.isBlocked(userDetails.getUsername(), recipientUsername) ||
            blockService.isBlocked(recipientUsername, userDetails.getUsername())) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You cannot share a post with this user"));
        }

        MessageResponseDTO message = chatService.sharePost(
                conversationId, userDetails.getUsername(), postId);

        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId, message);

        notificationService.createNotification(
                recipientUsername,
                userDetails.getUsername(),
                NotificationType.MESSAGE,
                null);

        return ResponseEntity.ok(message);
    }
}
