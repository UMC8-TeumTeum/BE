package umc.teumteum.server.domain.friend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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
}
