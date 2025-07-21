package umc.teumteum.server.domain.home.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.response.WishInfoResponseDTO;
import umc.teumteum.server.domain.home.dto.request.WishRequestDTO;
import umc.teumteum.server.domain.home.dto.response.WishlistResponseDTO;
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

    // Wish -> WishInfoResponseDTO
    public WishInfoResponseDTO toWishInfoDTO(Wish wish) {
        // WishCategory에서 Category 정보를 추출
        List<WishInfoResponseDTO.CategoryDTO> categoryDTOS = wish.getWishCategories().stream()
                .map(wc -> WishInfoResponseDTO.CategoryDTO.builder()
                        .id(wc.getCategory().getId())
                        .name(wc.getCategory().getName())
                        .build())
                .toList();

        return WishInfoResponseDTO.builder()
                .title(wish.getTitle())
                .content(wish.getContent())
                .estimatedDuration(wish.getEstimatedDuration())
                .categories(categoryDTOS)
                .build();
    }

    // wish 엔티티 목록 -> wishDTO 목록
    public static List<WishlistResponseDTO.WishDTO> toWishDTOList(List<Wish> wishes) {
        return wishes.stream()
                .map(wish -> WishlistResponseDTO.WishDTO.builder()
                        .id(wish.getId())
                        .title(wish.getTitle())
                        .estimatedDuration(wish.getEstimatedDuration())
                        .build())
                .toList();
    }

    // 위시 목록 & 페이지 정보 -> WishlistResponseDTO
    public WishlistResponseDTO toWishlistResponseDTO(List<Wish> wishes, int pageNumber, int pageSize, boolean hasNext, boolean isFirst, boolean isLast) {
        return WishlistResponseDTO.builder()
                .wishlist(toWishDTOList(wishes))
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .isFirst(isFirst)
                .isLast(isLast)
                .build();
    }
}
