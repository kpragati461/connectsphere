package com.connectsphere.service;

import com.connectsphere.model.Block;
import com.connectsphere.model.User;
import com.connectsphere.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserService userService;
    private final FollowService followService;

    public boolean toggleBlock(String blockerUsername, String blockedUsername) {
        if (blockerUsername.equals(blockedUsername))
            throw new RuntimeException("You cannot block yourself");

        User blocker = userService.findByUsername(blockerUsername);
        User blocked = userService.findByUsername(blockedUsername);

        Optional<Block> existing = blockRepository
                .findByBlockerAndBlocked(blocker, blocked);
                if (blockerUsername.equals(blockedUsername))
    throw new IllegalArgumentException("You cannot block yourself");

        if (existing.isPresent()) {
            blockRepository.delete(existing.get());
            return false; // unblocked
        } else {
            blockRepository.save(Block.builder()
                    .blocker(blocker)
                    .blocked(blocked)
                    .build());

            // NEW — a block should end any existing follow relationship in
            // either direction, otherwise the "Following" button and
            // follower/following counts go stale the moment you block someone.
            followService.removeFollowRelationship(blockerUsername, blockedUsername);

            return true; // blocked
        }
    }

    public boolean isBlocked(String blockerUsername, String blockedUsername) {
        try {
            User blocker = userService.findByUsername(blockerUsername);
            User blocked = userService.findByUsername(blockedUsername);
            return blockRepository.existsByBlockerAndBlocked(blocker, blocked);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getBlockedUsernames(String username) {
        User blocker = userService.findByUsername(username);
        return blockRepository.findByBlocker(blocker)
                .stream()
                .map(b -> b.getBlocked().getUsername())
                .collect(Collectors.toList());
    }

    // NEW — the reverse of getBlockedUsernames: everyone who has blocked
    // `username`. Needed so the feed can be filtered symmetrically — without
    // this, a person you blocked could still see your posts in their own feed.
    public List<String> getBlockedByUsernames(String username) {
        User blocked = userService.findByUsername(username);
        return blockRepository.findByBlocked(blocked)
                .stream()
                .map(b -> b.getBlocker().getUsername())
                .collect(Collectors.toList());
    }
}
