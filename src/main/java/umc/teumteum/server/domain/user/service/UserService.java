package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserSearchResponseDto> searchUsersByKeyword(String keyword, Long userId);

    User findOrCreateUser(OAuthUserInfo userInfo);

    User createDevUser();

    Optional<User> findUser(Long userId);

    UserResponseDTO.MyPageDTO getMyPage(User user);
}
