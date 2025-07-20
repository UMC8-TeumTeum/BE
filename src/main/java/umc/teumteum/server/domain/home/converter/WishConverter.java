package umc.teumteum.server.domain.home.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.WishRequestDTO;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

@Component
public class WishConverter {

    // WishRequestDTO -> Wish
    public Wish toWish(WishRequestDTO dto, User user) {
        return Wish.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .estimatedDuration(dto.getEstimatedDuration())
                .user(user)
                .build();
    }

    // List<WishCategory> -> WishCategory
    public List<WishCategory> toWishCategories(Wish wish, List<Category> categories) {
        return categories.stream()
                .map(category -> WishCategory.builder()
                        .wish(wish)
                        .category(category)
                        .build())
                .toList();
    }
}
