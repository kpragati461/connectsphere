package com.connectsphere.controller;

import com.connectsphere.dto.ConversationResponseDTO;
import com.connectsphere.dto.MessageResponseDTO;
import com.connectsphere.dto.SendMessageRequest;
import com.connectsphere.model.Conversation;
import com.connectsphere.model.NotificationType;
import com.connectsphere.service.ChatService;
import com.connectsphere.service.NotificationService;
import com.connectsphere.model.NotificationType;

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

    // REST — get all conversations
    @GetMapping("/api/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                chatService.getConversations(userDetails.getUsername()));
    }

    // REST — start or get conversation with a user
    @PostMapping("/api/conversations/{username}")
    public ResponseEntity<Map<String, Long>> startConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username) {
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

    MessageResponseDTO message = chatService.saveMessage(
            conversationId, userDetails.getUsername(), req);

    // broadcast to WebSocket subscribers
    messagingTemplate.convertAndSend(
            "/topic/conversation." + conversationId, message);

    // notify the other user
    String recipientUsername = chatService
            .getOtherUsername(conversationId, userDetails.getUsername());

    notificationService.createNotification(
            recipientUsername,
            userDetails.getUsername(),
            NotificationType.MESSAGE,
            null);

    return ResponseEntity.ok(message);
}
}
