package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

public interface UserService {
    Long searchByNickname(String nickname, Long requesterId);

    List<PublicTodoResponseDto> getRecentPublicTodos(Long userId);

    List<PublicTodoResponseDto> getDailyPublicTodos(Long userId, String date);

    List<String> getTodoDatesOfMonth(Long userId, String month);

    User findOrCreateUser(OAuthUserInfo userInfo);

    String determineUserNextStep(User user);
}
