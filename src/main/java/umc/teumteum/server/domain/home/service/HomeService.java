package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.request.*;
import umc.teumteum.server.domain.home.dto.response.*;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface HomeService {
    // Todo 등록
    TodoIdResponseDto createTodo(TodoRequestDto dto, User user);

    // Todo(Schedule) 조회
    TodoInfoResponseDto getTodoInfo(Long scheduleId);

    // Todo(Schedule) 수정
    TodoIdResponseDto updateTodoInfo(TodoRequestDto dto, Long scheduleId, User user);

    // Todo(Schedule) 삭제
    void deleteTodo(Long scheduleId);

    // Wish 등록
    void createWish(WishRequestDto dto, User user);

    // Wish 조회
    WishInfoResponseDto getWishInfo(Long wishId);

    // Wish 삭제
    void deleteWishByIds(WishDeleteRequestDto dto);

    // Wish 수정
    void updateWishInfo(WishRequestDto dto, Long wishId, User user);

    // Wishlist 조회
    WishlistResponseDto getWishlist(String duration, Integer page, User user);

    // 오늘의 시간표 조회
    List<TodayScheduleResponseDto> getTodaySchedule(LocalDate today, User user);

    // Wish 투두 등록
    void assignWish(Long wishId, WishAssignRequestDto dto, User user);

    // 카테고리 정보 조회
    List<CategoryResponseDto> getCategory();

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
