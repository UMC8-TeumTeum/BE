package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.CreateTodoRequestDTO;
import umc.teumteum.server.domain.home.dto.CreateTodoResponseDTO;
import umc.teumteum.server.domain.home.dto.TodoInfoResponseDTO;
import umc.teumteum.server.domain.home.entity.Schedule;

public interface HomeService {
    // Todo 등록
    CreateTodoResponseDTO createTodo(CreateTodoRequestDTO dto);

    // Todo(Schedule) 조회
    TodoInfoResponseDTO getTodoInfo(Long scheduleId);
}
