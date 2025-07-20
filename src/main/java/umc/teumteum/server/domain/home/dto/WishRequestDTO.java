package umc.teumteum.server.domain.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "WishRequestDTO : 위시 등록 DTO")
public class WishRequestDTO {

    @NotBlank
    @Schema(description = "위시 제목", example = "string")
    private String title;

    @Schema(description = "상세 설명", example = "string")
    private String content;

    @NotNull
    @Schema(description = "예상 소요 시간", example = "MINUTES_10")
    private EstimatedDuration estimatedDuration;

    @NotEmpty
    @NotNull
    @Schema(description = "위시 카테고리", example = "[1]")
    private List<Long> categories;

    @Schema(description = "회원 ID", example = "1")
    private Long userId;
}
