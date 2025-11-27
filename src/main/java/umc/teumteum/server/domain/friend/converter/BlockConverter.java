package umc.teumteum.server.domain.friend.converter;

import umc.teumteum.server.domain.friend.dto.BlockResponseDto;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.user.entity.User;

public class BlockConverter {

    public static Block toBlock(User blocker, User blocked) {
        return Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
    }

    // 차단된 유저 목록 조회용 변환 메서드
    public static BlockResponseDto.BlockedFriend toBlockedFriendDto(User blockedUser, String profileImageUrl) {
        return BlockResponseDto.BlockedFriend.builder()
                .userId(blockedUser.getId())
                .nickname(blockedUser.getNickname())
                .job(blockedUser.getJob())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}