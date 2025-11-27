package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.user.entity.User;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    // 차단하기: 이미 차단한 유저인지 확인
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    // 차단 해제: 차단 데이터를 찾아서 삭제하기 위해 조회
    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);

    // 목록 조회: 내가 차단한 목록을 페이징(Slice)으로 조회
    @Query("SELECT b FROM Block b JOIN FETCH b.blocked WHERE b.blocker.id = :blockerId")
    Slice<Block> findByBlockerId(@Param("blockerId") Long blockerId, Pageable pageable);
}