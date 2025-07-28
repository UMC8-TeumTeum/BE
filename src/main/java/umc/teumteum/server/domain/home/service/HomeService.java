package umc.teumteum.server.domain.home.service;

import java.time.Duration;
import umc.teumteum.server.domain.home.dto.request.TodoRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishDeleteRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishRequestDto;
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
    TodoIdResponseDto updateTodoInfo(TodoRequestDto dto, Long scheduleId);

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
}
