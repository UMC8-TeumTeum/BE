package umc.teumteum.server.domain.friend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException; // import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.friend.converter.BlockConverter;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.friend.exception.FriendException;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.BlockRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockServiceImpl implements BlockService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;

    @Override
    @Transactional
    public void blockUser(User loginUser, Long targetUserId) {
        // 자기 자신 차단 불가
        if (loginUser.getId().equals(targetUserId)) {
            throw new FriendException(FriendErrorStatus.INVALID_SELF_REQUEST);
        }

        // 대상 유저 존재 확인
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new FriendException(FriendErrorStatus.USER_NOT_FOUND));

        // 1차 방어: 이미 차단했는지 확인
        if (blockRepository.existsByBlockerAndBlocked(loginUser, targetUser)) {
            throw new FriendException(FriendErrorStatus.ALREADY_BLOCKED);
        }

        Block block = BlockConverter.toBlock(loginUser, targetUser);

        try {
            // 2차 방어: DB 유니크 제약조건
            blockRepository.save(block);
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 인해 중복 저장이 시도되었을 경우
            throw new FriendException(FriendErrorStatus.ALREADY_BLOCKED);
        }
    }
}