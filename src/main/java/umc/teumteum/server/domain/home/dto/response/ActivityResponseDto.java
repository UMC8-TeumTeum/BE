package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

import java.util.List;

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
  @Schema(description = " ai 위시 추천 응답")
  public static class AiWishResponse {

    @Schema(description = "추천된 ai 위시 목록")
      private List<AiWishDto> aiContents;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "추천된 위시 정보")
  public static class WishDto {

    @Schema(description = "Wish ID", example = "1")
    private Long id;

    @Schema(description = "위시 제목", example = "성북천 산책하기")
    private String title;

    @Schema(description = "예상 소요 시간", example = "TEN_MINUTES")
    private EstimatedDuration estimatedDuration;

  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "추천된 Ai 위시 정보")
  public static class AiWishDto {

    @Schema(description = "Wish ID", example = "3b4b2df3-8ef0-43e7-9474-762a76e53e49")
    private String id;

    @Schema(description = "위시 제목", example = "성북천 산책하기")
    private String title;

    @Schema(description = "위시 내용", example = "성북천을 따라 걷으며 물소리와 자연을 느끼고, 도심 속에서도 여유롭게 힐링할 수 있는 산책 활동입니다.")
    private String content;

    @Schema(description = "예상 소요 시간", example = "TEN_MINUTES")
    private EstimatedDuration estimatedDuration;

  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Ai 위시 저장 DTO")
  public static class AiSaveResponse {
    @Schema(description = "scheduleId" , example = "1")
    private Long id;
  }

}
