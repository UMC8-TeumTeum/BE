package umc.teumteum.server.domain.fcm.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.user.entity.User;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByToken(String token);

  Optional<FcmToken> findByTokenAndUser(String fcmToken, User user);

  List<FcmToken> findByUserAndIsActiveTrue(User user);

  @Query("SELECT t FROM FcmToken t WHERE t.isActive = true AND t.user IN :users")
  List<FcmToken> findActiveTokensByUsers(@Param("users") List<User> users);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM FcmToken t WHERE t.user.id = :userId")
  void deleteAllByUserId(@Param("userId") Long userId);
}
