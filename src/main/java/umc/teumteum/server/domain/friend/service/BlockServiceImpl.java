package umc.teumteum.server.domain.friend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.friend.converter.BlockConverter;
import umc.teumteum.server.domain.friend.dto.BlockResponseDto;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.friend.exception.FriendException;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.BlockRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.dto.PagingResponseDto;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockServiceImpl implements BlockService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final S3Util s3Util;

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

    @Override
    @Transactional
    public void unblockUser(User loginUser, Long targetUserId) {
        // 자기 자신 해제 불가
        if (loginUser.getId().equals(targetUserId)) {
            throw new FriendException(FriendErrorStatus.INVALID_SELF_REQUEST);
        }

        // 대상 유저 존재 확인
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new FriendException(FriendErrorStatus.USER_NOT_FOUND));

        // 차단 관계 조회
        Block block = blockRepository.findByBlockerAndBlocked(loginUser, targetUser)
                .orElseThrow(() -> new FriendException(FriendErrorStatus.NOT_BLOCKED));

        // 삭제
        blockRepository.delete(block);
    }

    @Override
    public PagingResponseDto<BlockResponseDto.BlockedFriend> getBlockedUsers(User loginUser, int page, int size) {
        // 가나다순(닉네임 오름차순) 정렬 적용
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("blocked.nickname").ascending());

        // DB 조회
        Slice<Block> slice = blockRepository.findByBlockerId(loginUser.getId(), pageable);

        // Converter를 사용하여 DTO 변환
        List<BlockResponseDto.BlockedFriend> dtoList = slice.getContent().stream()
                .map(block -> {
                    User blockedUser = block.getBlocked();
                    String profileUrl = toProfileUrl(blockedUser);
                    return BlockConverter.toBlockedFriendDto(blockedUser, profileUrl);
                })
                .collect(Collectors.toList());

        return new PagingResponseDto<>(dtoList, slice.hasNext());
    }

    // 프로필 이미지 URL 변환 헬퍼 메서드
    private String toProfileUrl(User user) {
        if (user == null || user.getProfileImageName() == null) {
            return null;
        }

        return s3Util.toPresignedUrl("profile/" + user.getProfileImageName(), Duration.ofMinutes(30));
    }

}