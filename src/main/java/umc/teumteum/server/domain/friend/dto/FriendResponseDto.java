package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FriendResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "FollowerFriend : 팔로워 유저 응답 DTO")
    public static class FollowerFriend {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "틈틈")
        private String nickname;

        @Schema(description = "분야 및 직종", example = "백엔드 개발자")
        private String job;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "FollowingFriend : 팔로잉 유저 응답 DTO")
    public static class FollowingFriend {

        @Schema(description = "유저 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "틈틈")
        private String nickname;

        @Schema(description = "분야 및 직종", example = "백엔드 개발자")
        private String job;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;

        @Schema(description = "즐겨찾기 여부", example = "true")
        private Boolean isFavorite;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "FriendProfile : 친구 프로필 응답")
    public static class FriendProfile {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이름", example = "틈틈")
        private String name;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;

        @Schema(description = "분야/직종", example = "백엔드 개발자")
        private String field;

        @Schema(description = "팔로잉 여부", example = "true")
        private boolean isFollowing;

        @Schema(description = "즐겨찾기 여부", example = "false")
        private boolean isFavorite;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "맞팔로우 친구 목록 조회 응답")
    public static class MutualFriend {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "틈틈")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "팔로잉 즐겨찾기 설정/해제 Response")
    public static class FriendFavorite {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "현재 즐겨찾기 상태", example = "true")
        private Boolean isFavorite;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "FriendPublicTodo : 공개 투두 응답")
    public static class FriendPublicTodo {

        @Schema(description = "투두 제목", example = "모여서 각자 코딩")
        private String title;

        @Schema(description = "시작 시간", example = "14:00")
        private String startTime;

        @Schema(description = "종료 시간", example = "16:00")
        private String endTime;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "FriendTeumTime : 친구 빈틈 시간 응답")
    public static class FriendTeumTime {

        @Schema(description = "일 단위 빈틈 시간", example = "1")
        private int days;

        @Schema(description = "시간 단위 빈틈 시간", example = "2")
        private int hours;

        @Schema(description = "분 단위 빈틈 시간", example = "30")
        private int minutes;

        @Schema(description = "총 빈틈 시간 (분)", example = "1620")
        private long totalMinutes;
    }
}

