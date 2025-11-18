package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequestDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "마이페이지 - 개인정보 수정")
    public static class ProfileRequest {

        @NotBlank(message = "닉네임은 필수 입력입니다")
        @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다")
        @Pattern(regexp = "^[a-zA-Z가-힣]*$", message = "닉네임은 영어와 한글만 가능합니다")
        @Schema(description = "사용자 닉네임 (최대 10자, 영어&한글만, 중복 불가)", example = "틈틈")
        private String nickname;

        @NotBlank(message = "분야/직종은 필수 입력입니다")
        @Size(max = 10, message = "분야/직종은 최대 10자까지 가능합니다")
        @Schema(description = "사용자의 분야/직종 (최대 10자)", example = "개발자")
        private String jobField;

        @NotNull(message = "빈틈 시간 공개 여부는 필수 입력입니다")
        @Schema(description = "빈틈 시간 공개 여부", example = "true")
        private Boolean timePublic;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "마이페이지 - 알림 설정 수정")
    public static class NotificationSettingRequest {

        @NotNull
        @Schema(description = "오늘의 일정 - 오전 9시 투두 일림 여부", example = "true")
        private Boolean todayTodo;

        @NotNull
        @Schema(description = "리마인드 알림 - 투두에 등록한 리마인드 알림 여부", example = "true")
        private Boolean remindAlarm;

        @NotNull
        @Schema(description = "새로운 팔로워 - 나를 팔로우한 새로운 친구가 있을 때", example = "true")
        private Boolean follow;

        @NotNull
        @Schema(description = "틈 요청 - 맞팔로우한 사용자의 틈 요청 알림", example = "true")
        private Boolean teum;
    }
}
