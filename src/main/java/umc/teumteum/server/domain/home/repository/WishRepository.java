package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.user.entity.User;

public interface WishRepository extends JpaRepository<Wish, Long> {
    boolean existsByUserAndTitleAndEstimatedDuration(User user, String title, EstimatedDuration estimatedDuration);
}
