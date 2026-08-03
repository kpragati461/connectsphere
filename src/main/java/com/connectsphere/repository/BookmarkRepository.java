package com.connectsphere.repository;

import com.connectsphere.model.Bookmark;
import com.connectsphere.model.Post;
import com.connectsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);
}
