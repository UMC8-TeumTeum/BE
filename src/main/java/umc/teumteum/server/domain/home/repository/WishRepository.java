package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

public interface WishRepository extends JpaRepository<Wish, Long> {
    boolean existsByUserAndTitleAndContentAndEstimatedDuration(User user, String title, String content, EstimatedDuration estimatedDuration);
    List<Wish> findByUserAndTitleAndContentAndEstimatedDuration(
            User user,
            String title,
            String content,
            EstimatedDuration estimatedDuration
    );
}
