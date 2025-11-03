package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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

    @Schema(description = "현재 위치 Id", example = "1")
    private Long locationId;

    @Schema(description = "사용자가 직접 입력한 위치", example = "회사")
    private String customLocation;

    @Schema(description = "활동 카테고리", example = "1")
    private Long categoryId;

    @Schema(description = "사용자가 직접 입력한 카테고리", example = "명상")
    private String customCategory;

  }
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AiWishSaveRequest {
    @NotBlank
    @Schema(description = "ai 컨텐츠 ID", example = "3b4b2df3-8ef0-43e7-9474-762a76e53e49")
    private String id;

    @NotNull
    @Schema(description = "시작 시간", example = "2025-07-24T10:00")
    private LocalDateTime startTime;

    @NotNull
    @Schema(description = "종료 시간", example = "2025-07-24T11:00")
    private LocalDateTime endTime;

  }

}
