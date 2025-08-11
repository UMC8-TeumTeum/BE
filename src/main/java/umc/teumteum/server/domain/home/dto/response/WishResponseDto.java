package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

import java.util.List;

public class WishResponseDto {
    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(title = "CategoryDto : 카테고리 조회 응답 Dto ")
    public static class CategoryDto {
        @Schema(description = "카테고리 ID" , example = "1")
        private Long categoryId;
        @Schema(description = "카테고리 이름" , example = "자기계발")
        private String categoryName;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(title = "WishInfoDTO : 위시 정보 조회 응답 DTO")
    public static class WishInfoDto {
        @Schema(description = "위시 제목", example = "수영하기")
        private String title;
        @Schema(description = "상세 설명", example = "수영수영")
        private String content;
        @Schema(description = "예상 소요 시간", example = "10m")
        private EstimatedDuration estimatedDuration;
        @Schema(description = "위시 카테고리", example = "{\"id\": 1, \"name\": \"자기계발\"}")
        private List<WishResponseDto.WishCategoryDto> categories;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WishCategoryDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(title = "WishlistDTO : 위시리스트 조회 응답 DTO")
    public static class WishlistDto {
        @Schema(description = "위시 목록", example = "[{ \"id\": 1, \"title\": \"수영하기\", \"estimatedDuration\": \"10m\" }]")
        private List<WishResponseDto.WishDto> wishlist;
        @Schema(description = "페이지 번호", example = "1")
        private int pageNumber;
        @Schema(description = "페이지 사이즈", example = "10")
        private int pageSize;
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;
        @Schema(description = "첫번째 페이지 여부", example = "true")
        private Boolean isFirst;
        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WishDto {
        private Long id;
        private String title;
        private EstimatedDuration estimatedDuration;
    }
}
