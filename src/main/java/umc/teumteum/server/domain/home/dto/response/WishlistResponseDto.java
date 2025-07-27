package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "WishlistResponseDTO : 위시리스트 조회 응답 DTO")
public class WishlistResponseDto {

    @Schema(description = "위시 목록", example = "[{ id: 1, title: 'string', estimatedDuration: '10m' }]")
    private List<WishDTO> wishlist;

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


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WishDTO {
        private Long id;
        private String title;
        private EstimatedDuration estimatedDuration;
    }
}
