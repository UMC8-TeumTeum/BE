package umc.teumteum.server.domain.friend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import umc.teumteum.server.domain.friend.dto.FavoriteResponseDto;
import umc.teumteum.server.domain.friend.dto.FriendMutualResponseDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    @Override
    @Transactional
    public Long follow(Long userId) {
        // TODO : 팔로우 로직 추후 구현
        return 123L;
    }

    @Override
    @Transactional
    public void unfollow(Long userId) {
        // TODO : 언팔로우 로직 추후 구현
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendMutualResponseDto> getMutualFriends() {
        // TODO : 맞팔로우 조회 로직 추후 구현
        return null;
    }

    @Override
    @Transactional
    public FavoriteResponseDto updateFavorite(Long userId, Boolean isFavorite) {
        // TODO : 즐겨찾기 로직 추후 구현
        return new FavoriteResponseDto(userId, isFavorite);
    }
}
