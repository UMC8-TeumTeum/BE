package umc.teumteum.server.domain.fcm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.user.entity.User;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByToken(String token);

  Optional<FcmToken> findByTokenAndUser(String fcmToken, User user);
}
