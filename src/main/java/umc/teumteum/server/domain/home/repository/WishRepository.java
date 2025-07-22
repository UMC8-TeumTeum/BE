package umc.teumteum.server.domain.home.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

public interface WishRepository extends JpaRepository<Wish, Long> {
    List<Wish> findByUserAndTitleAndContentAndEstimatedDuration(
            User user,
            String title,
            String content,
            EstimatedDuration estimatedDuration
    );

    Slice<Wish> findAllByUser(User user, Pageable pageable);
    Slice<Wish> findAllByUserAndEstimatedDuration(User user, EstimatedDuration estimatedDuration, Pageable pageable);
}
