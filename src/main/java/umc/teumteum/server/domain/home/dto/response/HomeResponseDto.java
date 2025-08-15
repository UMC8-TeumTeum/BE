package umc.teumteum.server.domain.home.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.global.util.DateTimeSerializer;
import umc.teumteum.server.global.util.TimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
  @Schema(title = "CalendarDto : 캘린더 조회 응답 Dto")
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
  @Schema(title = "TodolistDto : 투두리스트 응답 Dto")
  public static class TodolistDto{
    @Schema(description = "아이디", example = "1")
    private Long id;
    @Schema(description = "제목", example = "수영하기")
    private String title;
    @Schema(description = "시작시간", example = "01:00")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime startTime;
    @Schema(description = "종료시간", example = "02:00")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime endTime;
    @Schema(description = "공개여부", example = "false")
    private Boolean isPublic;
    @Schema(description = "리마인드 알림 여부", example = "ACTIVE")
    private AlarmStatus alarmStatus;
    @Schema(description = "타입", example = "ROUTINE")
    private ScheduleType type;
  }

  @Builder
  @Getter
  @AllArgsConstructor
  public static class VirtualRoutineDto {
    private LocalDate date;
    private Long routineId;
  }

  @Builder
  @Getter
  @AllArgsConstructor
  @Schema(title = "ReminderDto : 온보딩 리망니드 알림 정보 응답 Dto")
  public static class ReminderDto {
    @Schema(description = "온보딩 리마인드 알림 설정 정보", example = "[1,5]")
    private List<Integer> reminders;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @Schema(title = "TodayScheduleDto : 오늘의 빈틈 조회 응답 Dto")
  public static class TodayScheduleDto {
    @Schema(description = "시작 시간", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:00")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime endTime;

    @Schema(description = "타입", example = "TODO | SLEEP | EMPTY")
    private String type;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @Schema(title = "TodoIdDto : 투두 등록 응답 Dto")
  public static class TodoIdDto {
    @Schema(description = "투두 Id" , example = "1")
    private Long todoId;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @Schema(title = "TodoInfoDto : 투두 정보 조회 응답 Dto")
  public static class TodoInfoDto {
    @Schema(description = "투두 타입", example = "TODO | WISH | TEUM | AI")
    private ScheduleType type;

    @Schema(description = "투두 제목", example = "string")
    private String title;

    @Schema(description = "시작 시간", example = "2025-07-24T10:00")
    private LocalDateTime startTime;

    @Schema(description = "종료 시간", example = "2025-07-24T10:00")
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime endTime;

    @Schema(description = "상세 설명", example = "string")
    private String description;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "빈틈 시간 포함 여부", example = "false")
    private Boolean includeTeum;

    @Schema(description = "리마인드 알림 목록", example = "{\"alarm\": 30, \"status\": \"INACTIVE\"}]")
    private List<ReminderAlarmDto> remindAlarm;

    @Schema(description = "유저 프로필", example = "[\"string\",\"string\"]")
    private List<String> profileUrl;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ReminderAlarmDto {
    private Integer alarm;
    private AlarmStatus status;
  }
}
