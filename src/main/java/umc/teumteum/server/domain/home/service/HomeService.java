package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.CreateTodoRequestDTO;
import umc.teumteum.server.domain.home.dto.CreateTodoResponseDTO;

public interface HomeService {
    CreateTodoResponseDTO createTodo(CreateTodoRequestDTO dto);
}
