package com.connectsphere.repository;

import com.connectsphere.model.Conversation;
import com.connectsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1 = :user1 AND c.user2 = :user2) OR " +
           "(c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Conversation> findByUsers(User user1, User user2);

    @Query("SELECT c FROM Conversation c WHERE c.user1 = :user OR c.user2 = :user")
    List<Conversation> findByUser(User user);
}