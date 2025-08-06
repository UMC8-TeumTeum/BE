package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;

import java.time.LocalDate;
import java.time.LocalTime;

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

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TodolistDto{
    @Schema(description = "아이디", example = "1")
    private Long id;
    @Schema(description = "제목", example = "수영하기")
    private String title;
    @Schema(description = "시작시간", example = "01:00")
    private LocalTime startTime;
    @Schema(description = "종료시간", example = "02:00")
    private LocalTime endTime;
    @Schema(description = "공개여부", example = "false")
    private Boolean isPublic;
    @Schema(description = "리마인드 알림 여부", example = "ACTIVE")
    private String hasAlarm;
    @Schema(description = "타입", example = "ROUTINE")
    private ScheduleType type;
  }

  @Builder
  @Getter
  @AllArgsConstructor
  static public class VirtualRoutineDto {
    private LocalDate date;
    private Long routineId;
  }

}
