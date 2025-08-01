package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
  public static class OptionRequest {

    @NotNull
    @Schema(description = "예상 소요 시간", example = "10m")
    private EstimatedDuration estimatedDuration;

    @Schema(description = "활동 카테고리", example = "1")
    private Long categoryId;

    @Schema(description = "사용자가 직접 입력한 카테고리", example = "명상")
    private String customCategory;

  }

}
