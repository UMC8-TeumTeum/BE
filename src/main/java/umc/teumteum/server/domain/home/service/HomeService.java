package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.request.TodoRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishDeleteRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishRequestDTO;
import umc.teumteum.server.domain.home.dto.response.TodoIdResponseDTO;
import umc.teumteum.server.domain.home.dto.response.TodoInfoResponseDTO;
import umc.teumteum.server.domain.home.dto.response.WishlistResponseDTO;
import umc.teumteum.server.domain.home.dto.response.WishInfoResponseDTO;
import umc.teumteum.server.domain.user.entity.User;

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
    void createWish(WishRequestDTO dto,User user);

    // Wish 조회
    WishInfoResponseDTO getWishInfo(Long wishId);

    // Wish 삭제
    void deleteWishByIds(WishDeleteRequestDTO dto);

    // Wish 수정
    void updateWishInfo(WishRequestDTO dto, Long wishId,User user);

    // Wishlist 조회
    WishlistResponseDTO getWishlist(String duration, Integer page, User user);
}
