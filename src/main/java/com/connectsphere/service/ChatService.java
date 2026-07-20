package com.connectsphere.service;

import com.connectsphere.dto.ConversationResponseDTO;
import com.connectsphere.dto.MessageResponseDTO;
import com.connectsphere.dto.SendMessageRequest;
import com.connectsphere.model.Conversation;
import com.connectsphere.model.Message;
import com.connectsphere.model.User;
import com.connectsphere.repository.ConversationRepository;
import com.connectsphere.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    // get or create conversation between two users
    public Conversation getOrCreateConversation(String username1, String username2) {
        User user1 = userService.findByUsername(username1);
        User user2 = userService.findByUsername(username2);

        return conversationRepository.findByUsers(user1, user2)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .user1(user1)
                                .user2(user2)
                                .build()));
    }

    public MessageResponseDTO saveMessage(Long conversationId,
                                          String senderUsername,
                                          SendMessageRequest req) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        User sender = userService.findByUsername(senderUsername);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(req.getContent())
                .build();

        return mapToDTO(messageRepository.save(message));
    }

    public List<MessageResponseDTO> getMessages(Long conversationId, String username) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        messageRepository.markAsRead(conversation, username);
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ConversationResponseDTO> getConversations(String username) {
        User user = userService.findByUsername(username);
        return conversationRepository.findByUser(user)
                .stream()
                .map(c -> mapConversationToDTO(c, username))
                .collect(Collectors.toList());
    }

    private ConversationResponseDTO mapConversationToDTO(
            Conversation c, String currentUsername) {
        User other = c.getUser1().getUsername().equals(currentUsername)
                ? c.getUser2() : c.getUser1();

        String lastMsg = messageRepository.findLastMessage(c)
                .map(Message::getContent)
                .orElse("No messages yet");

        long unread = messageRepository
                .countByConversationAndReadFalseAndSenderUsernameNot(c, currentUsername);

        return new ConversationResponseDTO(
                c.getId(),
                other.getUsername(),
                other.getProfilePhoto(),
                lastMsg,
                c.getCreatedAt(),
                unread
        );
    }

    private MessageResponseDTO mapToDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}