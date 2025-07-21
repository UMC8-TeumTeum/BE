package umc.teumteum.server.domain.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "WishInfoResponseDTO : 위시 정보 조회 응답 DTO")
public class WishInfoResponseDTO {
    @Schema(description = "위시 제목", example = "string")
    private String title;

    @Schema(description = "상세 설명", example = "string")
    private String content;

    @Schema(description = "예상 소요 시간", example = "10m")
    private EstimatedDuration estimatedDuration;

    @Schema(description = "위시 카테고리", example = "[{ id: 1, name: '자기계발' }]")
    private List<CategoryDTO> categories;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CategoryDTO {
        private Long id;
        private String name;
    }
}
