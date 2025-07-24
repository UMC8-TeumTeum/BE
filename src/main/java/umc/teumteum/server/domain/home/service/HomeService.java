package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.request.TodoRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishDeleteRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishRequestDTO;
import umc.teumteum.server.domain.home.dto.response.*;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface HomeService {
    // Todo 등록
    TodoIdResponseDTO createTodo(TodoRequestDTO dto,User user);

    // Todo(Schedule) 조회
    TodoInfoResponseDTO getTodoInfo(Long scheduleId);

    // Todo(Schedule) 수정
    TodoIdResponseDTO updateTodoInfo(TodoRequestDTO dto, Long scheduleId);

    // Todo(Schedule) 삭제
    void deleteTodo(Long scheduleId);

    // Wish 등록
    void createWish(WishRequestDTO dto,User user);

    // Wish 조회
    WishInfoResponseDTO getWishInfo(Long wishId);

    // Wish 삭제
    void deleteWishByIds(WishDeleteRequestDTO dto);

    // Wish 수정
    void updateWishInfo(WishRequestDTO dto, Long wishId,User user);

    // Wishlist 조회
    WishlistResponseDTO getWishlist(String duration, Integer page, User user);

    // 오늘의 시간표 조회
    List<TodayScheduleResponseDTO> getTodaySchedule(LocalDate today, User user);

    // Wish 투두 등록
    void assignWish(Long wishId, WishAssignRequestDTO dto, User user);
}
