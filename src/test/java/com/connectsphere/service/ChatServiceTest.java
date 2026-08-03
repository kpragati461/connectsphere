package com.connectsphere.service;

import com.connectsphere.dto.MessageResponseDTO;
import com.connectsphere.model.Conversation;
import com.connectsphere.model.Message;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import com.connectsphere.repository.ConversationRepository;
import com.connectsphere.repository.MessageRepository;
import com.connectsphere.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void sharePostCreatesMessageWithSharedPostId() {
        User sender = User.builder().id(1L).username("alice").build();
        User recipient = User.builder().id(2L).username("bob").build();
        Conversation conversation = Conversation.builder()
                .id(10L)
                .user1(sender)
                .user2(recipient)
                .build();
        Post post = Post.builder().id(99L).content("Hello world").user(sender).build();

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(userService.findByUsername("alice")).thenReturn(sender);
        when(postRepository.findById(99L)).thenReturn(Optional.of(post));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponseDTO response = chatService.sharePost(10L, "alice", 99L);

        assertNotNull(response);
        assertEquals(99L, response.getSharedPostId());
        assertTrue(response.getContent().contains("Shared a post"));
        assertEquals("alice", response.getSenderUsername());
    }
}
