package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

public class ActivityRequestDto {
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WishOptionRequest {

    @NotNull
    @Schema(description = "예상 소요 시간", example = "10m")
    private EstimatedDuration estimatedDuration;

    @Schema(description = "활동 카테고리", example = "1")
    private Long categoryId;

    @Schema(description = "사용자가 직접 입력한 카테고리", example = "명상")
    private String customCategory;

  }
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AiWishOptionRequest {

    @NotNull
    @Schema(description = "예상 소요 시간", example = "10m")
    private EstimatedDuration estimatedDuration;

    @Schema(description = "현재 위치", example = "회사")
    private String location;

    @Schema(description = "활동 카테고리", example = "1")
    private Long categoryId;

    @Schema(description = "사용자가 직접 입력한 카테고리", example = "명상")
    private String customCategory;

  }

}
