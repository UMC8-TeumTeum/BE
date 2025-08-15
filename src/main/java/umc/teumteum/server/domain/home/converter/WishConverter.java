package umc.teumteum.server.domain.home.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishSaveRequest;
import umc.teumteum.server.domain.home.dto.request.WishRequestDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto;
import umc.teumteum.server.domain.home.dto.response.CategoryResponseDto;
import umc.teumteum.server.domain.home.dto.response.WishInfoResponseDto;
import umc.teumteum.server.domain.home.dto.response.WishlistResponseDto;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

@Component
public class WishConverter {

    // WishRequestDTO -> Wish
    public Wish toWish(WishRequestDto dto, User user) {
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
    public WishInfoResponseDto toWishInfoDTO(Wish wish) {
        // WishCategory에서 Category 정보를 추출
        List<WishInfoResponseDto.CategoryDTO> categoryDTOS = wish.getWishCategories().stream()
                .map(wc -> WishInfoResponseDto.CategoryDTO.builder()
                        .id(wc.getCategory().getId())
                        .name(wc.getCategory().getName())
                        .build())
                .toList();

        return WishInfoResponseDto.builder()
                .title(wish.getTitle())
                .content(wish.getContent())
                .estimatedDuration(wish.getEstimatedDuration())
                .categories(categoryDTOS)
                .build();
    }

    // wish 엔티티 목록 -> wishDTO 목록
    public static List<WishlistResponseDto.WishDTO> toWishDTOList(List<Wish> wishes) {
        return wishes.stream()
                .map(wish -> WishlistResponseDto.WishDTO.builder()
                        .id(wish.getId())
                        .title(wish.getTitle())
                        .estimatedDuration(wish.getEstimatedDuration())
                        .build())
                .toList();
    }

    // 위시 목록 & 페이지 정보 -> WishlistResponseDTO
    public WishlistResponseDto toWishlistResponseDTO(List<Wish> wishes, int pageNumber, int pageSize, boolean hasNext, boolean isFirst, boolean isLast) {
        return WishlistResponseDto.builder()
                .wishlist(toWishDTOList(wishes))
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .isFirst(isFirst)
                .isLast(isLast)
                .build();
    }

    public List<CategoryResponseDto> toCategoryResponseDTO(List<Category> categories) {
        return categories.stream()
                .map(category -> CategoryResponseDto.builder()
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
    public Schedule toScheduleFromAiWish(User user, AiWishSaveRequest request, String title){
        return Schedule.builder()
            .user(user)
            .title(title)
            .description(null)
            .type(ScheduleType.AI)
            .date(request.getStartTime().toLocalDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .build();
    }
}
