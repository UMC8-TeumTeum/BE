package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;

import java.util.List;

public interface UserService {
    Long searchByNickname(String nickname);

    List<PublicTodoResponseDto> getRecentPublicTodos(Long userId);

    List<PublicTodoResponseDto> getDailyPublicTodos(Long userId, String date);

    List<String> getTodoDatesOfMonth(Long userId, String month);
}
