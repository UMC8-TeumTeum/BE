package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.user.entity.User;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    // 차단 여부 확인용
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    // 삭제를 위해 Block 엔티티를 조회
    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);
}