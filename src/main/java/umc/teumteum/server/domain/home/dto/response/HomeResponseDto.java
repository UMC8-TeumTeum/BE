package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class HomeResponseDto {

  @Getter
  @Builder
  @AllArgsConstructor
  public static class TeumTimeDto {

    @Schema(description = "총 틈 활동 시간(분단위)", example = "2940")
    private Long totalMinutes;
    @Schema(description = "일(day) 단위 시간", example = "1")
    private Integer days;
    @Schema(description = "시간(hour) 단위", example = "20")
    private Integer hours;
    @Schema(description = "분(minutes) 단위", example = "30")
    private Integer minutes;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class CalendarDto{

    @Schema(description = "날짜", example = "2025-07-31")
    private LocalDate date;

    @Schema(description = "스케줄 여부", example = "true")
    private Boolean hasSchedule;
  }

}
