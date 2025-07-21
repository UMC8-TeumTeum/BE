package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
