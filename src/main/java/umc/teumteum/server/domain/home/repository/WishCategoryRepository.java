package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;

public interface WishCategoryRepository extends JpaRepository<WishCategory, Long> {
    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query("DELETE FROM WishCategory wc WHERE wc.wish.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
