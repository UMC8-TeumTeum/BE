package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    List<User> findByStatus(UserStatus status);

    // 닉네임 검색 시 차단 관계(내가 차단함 OR 나를 차단함)를 제외하고 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.nickname LIKE %:keyword% " +
            "AND u.id <> :loginUserId " + // 내 자신 제외
            "AND NOT EXISTS (SELECT b FROM Block b WHERE b.blocker.id = :loginUserId AND b.blocked = u) " + // 내가 차단한 사람 제외
            "AND NOT EXISTS (SELECT b FROM Block b WHERE b.blocked.id = :loginUserId AND b.blocker = u)")   // 나를 차단한 사람 제외
    List<User> searchUserWithBlockCheck(
            @Param("keyword") String keyword,
            @Param("loginUserId") Long loginUserId
    );
}