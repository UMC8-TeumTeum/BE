package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.*;

public interface HomeService {
    // Todo 등록
    TodoIdResponseDTO createTodo(TodoRequestDTO dto);

    // Todo(Schedule) 조회
    TodoInfoResponseDTO getTodoInfo(Long scheduleId);

    // Todo(Schedule) 수정
    TodoIdResponseDTO updateTodoInfo(TodoRequestDTO dto, Long scheduleId);

    // Todo(Schedule) 삭제
    void deleteTodo(Long scheduleId);

    // Wish 등록
    void createWish(WishRequestDTO dto);

    // Wish 조회
    WishInfoResponseDTO getWishInfo(Long wishId);

    // Wish 삭제
    void deleteWishByIds(WishDeleteRequestDTO dto);

    // Wish 수정
    void updateWishInfo(WishRequestDTO dto, Long wishId);
}
