package com.connectsphere.repository;

import com.connectsphere.model.Conversation;
import com.connectsphere.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    long countByConversationAndReadFalseAndSenderUsernameNot(
            Conversation conversation, String username);

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastMessage(Conversation conversation);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true WHERE m.conversation = :conversation " +
           "AND m.sender.username != :username")
    void markAsRead(Conversation conversation, String username);
}