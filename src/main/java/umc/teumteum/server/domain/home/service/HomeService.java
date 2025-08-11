package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.request.*;
import umc.teumteum.server.domain.home.dto.response.*;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface HomeService {
    // Todo 등록
    HomeResponseDto.TodoIdDto createTodo(HomeRequestDto.TodoRequestDto dto, User user);

    // Todo(Schedule) 조회
    HomeResponseDto.TodoInfoDto getTodoInfo(Long scheduleId);

    // Todo(Schedule) 수정
    HomeResponseDto.TodoIdDto updateTodoInfo(HomeRequestDto.TodoRequestDto dto, Long scheduleId, User user);

    // Todo(Schedule) 삭제
    void deleteTodo(Long scheduleId);

    // Wish 등록
    void createWish(WishRequestDto.CreateDto dto, User user);

    // Wish 조회
    WishResponseDto.WishInfoDto getWishInfo(Long wishId);

    // Wish 삭제
    void deleteWishByIds(WishRequestDto.WishDeleteDto dto);

    // Wish 수정
    void updateWishInfo(WishRequestDto.CreateDto dto, Long wishId);

    // Wishlist 조회
    WishResponseDto.WishlistDto getWishlist(String duration, Integer page, User user);

    // 오늘의 시간표 조회
    List<HomeResponseDto.TodayScheduleDto> getTodaySchedule(LocalDate today, User user);

    // Wish 투두 등록
    void assignWish(Long wishId, WishRequestDto.WishAssignDto dto, User user);

    // 카테고리 정보 조회
    List<WishResponseDto.CategoryDto> getCategory();

    // 빈틈 시간 조회
    HomeResponseDto.TeumTimeDto getTeaumTime(User user);

    // 캘린더 스케줄 여부 조회
    List<HomeResponseDto.CalendarDto> getCalendar(LocalDate startDate, LocalDate endDate,User user);

    // 투두리스트 조회
    List<HomeResponseDto.TodolistDto> getTodolist(LocalDate date,User user);

    // 가상의 루틴 ID 파싱
    HomeResponseDto.VirtualRoutineDto getVirtualRoutine(Long virtualId);

    // 리마인드 알림 정보 조회
    HomeResponseDto.ReminderDto getUserRemind(User user);

    // 리마인드 알림 정보 변경
    void updateAlarm(HomeRequestDto.AlarmDto dto);
}
