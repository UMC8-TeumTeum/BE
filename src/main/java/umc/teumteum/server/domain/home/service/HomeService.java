package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.TodoRequestDTO;
import umc.teumteum.server.domain.home.dto.TodoIdResponseDTO;
import umc.teumteum.server.domain.home.dto.TodoInfoResponseDTO;

public interface HomeService {
    // Todo 등록
    TodoIdResponseDTO createTodo(TodoRequestDTO dto);

    // Todo(Schedule) 조회
    TodoInfoResponseDTO getTodoInfo(Long scheduleId);

    // Todo(Schedule) 수정
    TodoIdResponseDTO updateTodoInfo(TodoRequestDTO dto, Long scheduleId);

    // Todo(Schedule) 삭제
    void deleteTodo(Long scheduleId);
}
