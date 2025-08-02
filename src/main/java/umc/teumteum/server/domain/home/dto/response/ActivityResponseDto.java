package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

public class ActivityResponseDto {

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "위시 추천 응답")
  public static class WishResponse {

    @Schema(description = "추천된 위시 목록")
    private List<WishDto> wishes;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "추천된 위시 정보")
  public static class WishDto {

    @Schema(description = "Wish ID", example = "1")
    private Long id;

    @Schema(description = "위시 상세 설명", example = "성북천 산책하기")
    private String content;

    @Schema(description = "예상 소요 시간", example = "TEN_MINUTES")
    private EstimatedDuration estimatedDuration;

  }

}
