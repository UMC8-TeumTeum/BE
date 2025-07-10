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
        // TODO : 실제 로직 구현
        return 123L;
    }
}
