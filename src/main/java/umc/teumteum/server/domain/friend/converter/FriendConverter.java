package umc.teumteum.server.domain.friend.converter;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.friend.dto.FollowerUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FollowingUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FriendProfileResponseDto;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendConverter {

    private final S3Util s3Util;

    public FollowingUserResponseDto toFollowingUserResponse(Friend friend) {
        User following = friend.getFollowing();
        String imageUrl = s3Util.toPresignedUrl("profile/" + following.getProfileImageName(), Duration.ofMinutes(30));

        return new FollowingUserResponseDto(
                following.getId(),
                following.getNickname(),
                imageUrl,
                friend.getIsFavorite()
        );
    }

    public List<FollowingUserResponseDto> toFollowingUserResponseList(List<Friend> friendList) {
        return friendList.stream()
                .map(this::toFollowingUserResponse)
                .collect(Collectors.toList());
    }

    public FollowerUserResponseDto toFollowerUserResponse(Friend friend) {
        User follower = friend.getFollower();
        String imageUrl = s3Util.toPresignedUrl("profile/" + follower.getProfileImageName(), Duration.ofMinutes(30));

        return new FollowerUserResponseDto(
                follower.getId(),
                follower.getNickname(),
                imageUrl
        );
    }

    public List<FollowerUserResponseDto> toFollowerUserResponseList(List<Friend> friendList) {
        return friendList.stream()
                .map(this::toFollowerUserResponse)
                .collect(Collectors.toList());
    }

    public FriendProfileResponseDto toFriendProfileResponse(User targetUser, Friend followRelation) {
        String imageUrl = s3Util.toPresignedUrl("profile/" + targetUser.getProfileImageName(), Duration.ofMinutes(30));

        return FriendProfileResponseDto.builder()
                .userId(targetUser.getId())
                .name(targetUser.getNickname())
                .profileImageUrl(imageUrl)
                .field(targetUser.getJob()) // 또는 getField() 등
                .isFollowing(followRelation != null)
                .isFavorite(followRelation != null && followRelation.getIsFavorite())
                .build();
    }

    public Long calculateTeumTime(List<Schedule> schedules) {
        return schedules.stream()
                .mapToLong(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes())
                .sum();
    }

}
