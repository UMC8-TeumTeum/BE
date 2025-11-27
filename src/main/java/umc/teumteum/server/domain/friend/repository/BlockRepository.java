package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.user.entity.User;

public interface BlockRepository extends JpaRepository<Block, Long> {
    // 차단 여부 확인용
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
}