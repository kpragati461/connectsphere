package com.connectsphere.repository;

import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByFeedExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime now);
    List<Post> findByUserOrderByCreatedAtDesc(User user);
}