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

    public boolean toggleBlock(String blockerUsername, String blockedUsername) {
        if (blockerUsername.equals(blockedUsername))
            throw new RuntimeException("You cannot block yourself");

        User blocker = userService.findByUsername(blockerUsername);
        User blocked = userService.findByUsername(blockedUsername);

        Optional<Block> existing = blockRepository
                .findByBlockerAndBlocked(blocker, blocked);

        if (existing.isPresent()) {
            blockRepository.delete(existing.get());
            return false; // unblocked
        } else {
            blockRepository.save(Block.builder()
                    .blocker(blocker)
                    .blocked(blocked)
                    .build());
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
}