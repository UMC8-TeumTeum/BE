package umc.teumteum.server.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import umc.teumteum.server.domain.user.entity.enums.Weekday;

import java.time.LocalTime;
import java.util.List;

public class UserResponseDTO {

  @Getter
  @Builder
  @AllArgsConstructor
  public static class MyPageDTO {
    @Schema(description = "유저 ID", example = "1")
    private Long userId;
    @Schema(description = "닉네임", example = "고냐니")
    private String nickname;
    @Schema(description = "프로필 이미지 URL", example = "https://~")
    private String profileImageUrl;
    @Schema(description = "직종/분야", example = "개발자")
    private String job;


  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class RoutineDTO {

    @Schema(description = "반복일정 ID", example = "1")
    private Long routineId;

    @Schema(description = "제목", example = "매주 산책")
    private String title;

    @Schema(description = "상세 내용", example = "한강에서 산책")
    private String description;

    @Schema(description = "반복 요일", example = "WEDNESDAY")
    private Weekday weekday;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "시작 시간 (HH:mm 형식)", example = "13:00")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "종료 시간 (HH:mm 형식)", example = "00:00")
    private LocalTime endTime;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class RemindAlarmList {

    @Schema(description = "리마인드 알림 설정 (1, 3, 5, 10, 30, 빈 배열)", example = "[1, 5, 30]")
    private List<Integer> remindAlarms;
  }
}
