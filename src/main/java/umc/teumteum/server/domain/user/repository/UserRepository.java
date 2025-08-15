package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    List<User> findByNicknameContaining(String keyword);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    List<User> findByStatus(UserStatus status);
}