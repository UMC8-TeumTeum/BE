package umc.teumteum.server.domain.friend.converter;

import umc.teumteum.server.domain.friend.dto.FollowerUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FollowingUserResponseDto;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class FriendConverter {

    public static FollowingUserResponseDto toFollowingUserResponse(Friend friend) {
        User following = friend.getFollowing();
        String imageUrl = following.getProfileImageUrl(); // key → url 매핑

        return new FollowingUserResponseDto(
                following.getId(),
                following.getNickname(),
                imageUrl,
                friend.getIsFavorite()
        );
    }

    public static List<FollowingUserResponseDto> toFollowingUserResponseList(List<Friend> friendList) {
        return friendList.stream()
                .map(FriendConverter::toFollowingUserResponse)
                .collect(Collectors.toList());
    }

    public static FollowerUserResponseDto toFollowerUserResponse(Friend friend) {
        User follower = friend.getFollower();
        String imageUrl = follower.getProfileImageUrl();

        return new FollowerUserResponseDto(
                follower.getId(),
                follower.getNickname(),
                imageUrl
        );
    }

    public static List<FollowerUserResponseDto> toFollowerUserResponseList(List<Friend> friendList) {
        return friendList.stream()
                .map(FriendConverter::toFollowerUserResponse)
                .collect(Collectors.toList());
    }

}
