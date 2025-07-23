package umc.teumteum.server.domain.user.service;

import jakarta.validation.Valid;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.dto.UserRequestDTO;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserSearchResponseDto> searchUsersByKeyword(String keyword, Long userId);

    List<PublicTodoResponseDto> getRecentPublicTodos(Long userId);

    List<PublicTodoResponseDto> getDailyPublicTodos(Long userId, String date);

    List<String> getTodoDatesOfMonth(Long userId, String month);

    User findOrCreateUser(OAuthUserInfo userInfo);

    User createDevUser();

    Optional<User> findUser(Long userId);

    void saveAgreement(UserRequestDTO.AgreeRequest request, User user);

    void saveNicknameAndJob(UserRequestDTO.NicknameJobRequest request, User user);

    UserResponseDTO.MyPageDTO getMyPage(User user);
}
