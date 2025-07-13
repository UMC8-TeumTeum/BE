package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;

import java.util.List;

public class UserServiceImpl implements UserService {
    @Override
    public UserSearchResponseDto searchByNickname(String nickname) {
        // TODO : 닉네임으로 사용자 검색 로직 구현
        return null;
    }

    @Override
    public List<PublicTodoResponseDto> getRecentPublicTodos(Long userId) {
        // TODO : 최근 공개 투두 2개 조회 로직 구현
        return List.of();
    }
}
