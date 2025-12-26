package umc.teumteum.server.domain.home.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // 시간 + 카테고리 이름 일치
    @Query("SELECT w FROM Wish w " +
        "JOIN w.wishCategories wc " +
        "JOIN wc.category c " +
        "WHERE w.user = :user AND w.estimatedDuration = :duration AND LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
    List<Wish> findByUserAndDurationAndWishCategoriesLike(User user, EstimatedDuration duration, String categoryName);

    // 시간만 일치
    List<Wish> findByUserAndEstimatedDuration(User user, EstimatedDuration duration);

    // 카테고리 이름만 일치
    @Query("SELECT w FROM Wish w " +
        "JOIN w.wishCategories wc " +
        "JOIN wc.category c " +
        "WHERE w.user = :user AND LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
    List<Wish> findByUserAndCategoryLike(User user, String categoryName);

    // userId로 조회
    List<Wish> findByUserId(Long userId);
}
