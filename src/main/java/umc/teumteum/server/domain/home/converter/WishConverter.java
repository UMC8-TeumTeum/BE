package umc.teumteum.server.domain.home.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.response.*;
import umc.teumteum.server.domain.home.dto.request.WishRequestDto;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

@Component
public class WishConverter {

    // CreateDto -> Wish
    public Wish toWish(WishRequestDto.CreateDto dto, User user) {
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

    // Wish -> WishInfoDto
    public WishResponseDto.WishInfoDto toWishInfoDTO(Wish wish) {
        // WishCategory에서 Category 정보를 추출
        List<WishResponseDto.WishCategoryDto> categoryDTOS = wish.getWishCategories().stream()
                .map(wc -> WishResponseDto.WishCategoryDto.builder()
                        .id(wc.getCategory().getId())
                        .name(wc.getCategory().getName())
                        .build())
                .toList();

        return WishResponseDto.WishInfoDto.builder()
                .title(wish.getTitle())
                .content(wish.getContent())
                .estimatedDuration(wish.getEstimatedDuration())
                .categories(categoryDTOS)
                .build();
    }

    // wish 엔티티 목록 -> WishDto 목록
    public static List<WishResponseDto.WishDto> toWishDTOList(List<Wish> wishes) {
        return wishes.stream()
                .map(wish -> WishResponseDto.WishDto.builder()
                        .id(wish.getId())
                        .title(wish.getTitle())
                        .estimatedDuration(wish.getEstimatedDuration())
                        .build())
                .toList();
    }

    // 위시 목록 & 페이지 정보 -> WishlistDto
    public WishResponseDto.WishlistDto toWishlistResponseDTO(List<Wish> wishes, int pageNumber, int pageSize, boolean hasNext, boolean isFirst, boolean isLast) {
        return WishResponseDto.WishlistDto.builder()
                .wishlist(toWishDTOList(wishes))
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .isFirst(isFirst)
                .isLast(isLast)
                .build();
    }

    public List<WishResponseDto.CategoryDto> toCategoryResponseDTO(List<Category> categories) {
        return categories.stream()
                .map(category -> WishResponseDto.CategoryDto.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .build())
                .toList();
    }

    public ActivityResponseDto.WishDto toActivityWishDto(Wish wish) {
        return ActivityResponseDto.WishDto.builder()
            .id(wish.getId())
            .title(wish.getTitle())
            .estimatedDuration(wish.getEstimatedDuration())
            .build();
    }

    public List<ActivityResponseDto.WishDto> toActivityWishDtoList(List<Wish> wishes) {
        return wishes.stream()
            .map(this::toActivityWishDto)
            .toList();
    }
}
