package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;

import java.util.List;

public interface UserService {
    UserSearchResponseDto searchByNickname(String nickname);

    List<PublicTodoResponseDto> getRecentPublicTodos(Long userId);

    List<PublicTodoResponseDto> getDailyPublicTodos(Long userId, String date);
}
