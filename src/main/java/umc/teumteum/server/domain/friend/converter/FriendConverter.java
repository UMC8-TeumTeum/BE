package umc.teumteum.server.domain.friend.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.util.TimeUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendConverter {

    private final S3Util s3Util;
    private final TimeUtil timeUtil;

    public FriendResponseDto.FollowingFriend toFollowingUserResponse(Friend friend, String profileImageUrl) {
        User following = friend.getFollowing();
        return new FriendResponseDto.FollowingFriend(
                following.getId(),
                following.getNickname(),
                following.getJob(),
                profileImageUrl,
                friend.getIsFavorite()
        );
    }

    public FriendResponseDto.FollowerFriend toFollowerUserResponse(Friend friend, String profileImageUrl) {
        User follower = friend.getFollower();
        return new FriendResponseDto.FollowerFriend(
                follower.getId(),
                follower.getNickname(),
                follower.getJob(),
                profileImageUrl
        );
    }


    public FriendResponseDto.FriendProfile toFriendProfileResponse(User targetUser, Friend followRelation, String profileImageUrl) {
        return FriendResponseDto.FriendProfile.builder()
                .userId(targetUser.getId())
                .name(targetUser.getNickname())
                .profileImageUrl(profileImageUrl)
                .field(targetUser.getJob())
                .isFollowing(followRelation != null)
                .isFavorite(followRelation != null && followRelation.getIsFavorite())
                .build();
    }

    public Long calculateTeumTime(List<Schedule> schedules) {
        return schedules.stream()
                .mapToLong(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes())
                .sum();
    }

    public FriendResponseDto.FriendTeumTime toFriendTeumTimeResponse(long totalMinutes) {
        int days = (int) (totalMinutes / (60 * 24));
        int hours = (int) ((totalMinutes % (60 * 24)) / 60);
        int minutes = (int) (totalMinutes % 60);
        return new FriendResponseDto.FriendTeumTime(days, hours, minutes, totalMinutes);
    }

    public List<FriendResponseDto.FriendPublicTodo> toFriendPublicTodoResponse(List<Schedule> schedules) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return schedules.stream()
                .map(s -> new FriendResponseDto.FriendPublicTodo(
                        s.getTitle(),
                        s.getStartTime().format(formatter),
                        timeUtil.parseAndFormatEndTime(s.getEndTime().toLocalTime())
                ))
                .collect(Collectors.toList());
    }

    public List<String> toDateStringList(List<LocalDate> dates) {
        return dates.stream()
                .map(LocalDate::toString)
                .toList();
    }

    // 친구 - 팔로우 관계 생성
    public static Friend toFriend(User follower, User following) {
        return Friend.builder()
                .follower(follower)
                .following(following)
                .build();
    }

    // 친구 - 맞팔로우 목록 조회 응답
    public static FriendResponseDto.MutualFriend toMutualFriendDto(User user, String profileImageUrl) {
        return FriendResponseDto.MutualFriend.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .build()
                ;
    }

    // 친구 - 즐겨찾기 설정/해제 응답
    public static FriendResponseDto.FriendFavorite toFriendFavoriteDto(User targetUser, Boolean isFavorite) {
        return FriendResponseDto.FriendFavorite.builder()
                .userId(targetUser.getId())
                .isFavorite(isFavorite)
                .build()
                ;
    }

    public List<FriendResponseDto.FriendPublicTodo> toFriendPublicTodoResponseList(List<Schedule> schedules) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return schedules.stream()
                .map(s -> new FriendResponseDto.FriendPublicTodo(
                        s.getTitle(),
                        s.getStartTime().format(formatter),
                        timeUtil.parseAndFormatEndTime(s.getEndTime().toLocalTime())
                ))
                .collect(Collectors.toList());
    }

}
