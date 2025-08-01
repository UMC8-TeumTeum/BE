package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;

public interface WishCategoryRepository extends JpaRepository<WishCategory, Long> {
}
