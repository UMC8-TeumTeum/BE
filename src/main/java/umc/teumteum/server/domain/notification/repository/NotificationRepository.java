package umc.teumteum.server.domain.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Slice<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
