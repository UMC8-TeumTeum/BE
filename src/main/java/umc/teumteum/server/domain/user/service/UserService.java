package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;

public interface UserService {
    UserSearchResponseDto searchByNickname(String nickname);
}
