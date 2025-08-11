package umc.teumteum.server.domain.home.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.global.util.TimeSerializer;

import java.time.LocalDate;
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
  @Schema(title = "CategoryDto : 카테고리 조회 응답 Dto ")
  public static class CategoryDto {
    @Schema(description = "카테고리 ID" , example = "1")
    private Long categoryId;
    @Schema(description = "카테고리 이름" , example = "자기계발")
    private String categoryName;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @Schema(title = "WishInfoDTO : 위시 정보 조회 응답 DTO")
  public static class WishInfoDto {
    @Schema(description = "위시 제목", example = "string")
    private String title;
    @Schema(description = "상세 설명", example = "string")
    private String content;
    @Schema(description = "예상 소요 시간", example = "10m")
    private EstimatedDuration estimatedDuration;
    @Schema(description = "위시 카테고리", example = "{\"id\": 1, \"name\": \"자기계발\"}")
    private List<HomeResponseDto.WishCategoryDto> categories;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class WishCategoryDto {
    private Long id;
    private String name;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @Schema(title = "WishlistDTO : 위시리스트 조회 응답 DTO")
  public static class WishlistDto {
    @Schema(description = "위시 목록", example = "[{ \"id\": 1, \"title\": \"수영하기\", \"estimatedDuration\": \"10m\" }]")
    private List<HomeResponseDto.WishDto> wishlist;
    @Schema(description = "페이지 번호", example = "1")
    private int pageNumber;
    @Schema(description = "페이지 사이즈", example = "10")
    private int pageSize;
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private Boolean hasNext;
    @Schema(description = "첫번째 페이지 여부", example = "true")
    private Boolean isFirst;
    @Schema(description = "마지막 페이지 여부", example = "false")
    private Boolean isLast;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WishDto {
    private Long id;
    private String title;
    private EstimatedDuration estimatedDuration;
  }
}
