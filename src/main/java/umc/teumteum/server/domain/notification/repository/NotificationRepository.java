package umc.teumteum.server.domain.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
