package umc.teumteum.server.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.user.entity.enums.Weekday;

import java.time.LocalTime;
import java.util.List;

public class OnboardingRequestDto {

    // 온보딩 - 약관 동의
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreeRequest {

        @NotNull(message = "서비스 이용약관 동의 여부를 입력해야 합니다")
        @Schema(description = "서비스 이용약관 동의 여부 (필수)", example = "true")
        private Boolean tosConsent;

        @NotNull(message = "개인정보 수집 및 이용 동의 여부를 입력해야 합니다")
        @Schema(description = "개인정보 수집 및 이용 동의 여부 (필수)", example = "true")
        private Boolean privacyConsent;

        @NotNull(message = "개인정보 제3자 제공 동의 여부를 입력해야 합니다")
        @Schema(description = "개인정보 제3자 제공 동의 여부 (선택)", example = "false")
        private Boolean thirdPartyConsent;

        @NotNull(message = "마케팅 정보 수신 동의 여부를 입력해야 합니다")
        @Schema(description = "마케팅 정보 수신 동의 여부 (선택)", example = "false")
        private Boolean marketingConsent;
    }


    // 온보딩 - 닉네임 & 분야/직종
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NicknameJobRequest {

        @NotBlank(message = "닉네임은 필수 입력입니다")
        @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다")
        @Pattern(regexp = "^[a-zA-Z가-힣]*$", message = "닉네임은 영어와 한글만 가능합니다")
        @Schema(description = "사용자 닉네임 (최대 10자, 영어&한글만, 중복 불가)", example = "틈틈")
        private String nickname;

        @NotBlank(message = "분야/직종은 필수 입력입니다")
        @Size(max = 10, message = "분야/직종은 최대 10자까지 가능합니다")
        @Schema(description = "사용자의 분야/직종 (최대 10자)", example = "개발자")
        private String jobField;
    }


    // 온보딩 - 프로필 이미지 업로드용 URl 발급
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileImagePresignedUrlRequest {

        @NotBlank
        @Schema(description = "업로드할 파일의 MIME 타입", example = "image/png")
        private String contentType;
    }


    // 온보딩 - 프로필 이미지
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileImageRequest {

        @NotBlank
        @Schema(description = "업로드한 파일의 이름", example = "550e8400-e29b.jpg")
        private String fileName;
    }


    // 온보딩 - 수면패턴
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SleepPatternRequest {

        @NotNull(message = "취침 시간은 필수 입력입니다")
        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "취침 시간", example = "02:00")
        private LocalTime sleepTime;

        @NotNull(message = "기상 시간은 필수 입력입니다")
        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "기상 시간", example = "11:30")
        private LocalTime wakeTime;
    }


    // 온보딩 - 반복 일정
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutineListRequest {

        @NotEmpty(message = "최소 1개 이상의 반복 일정이 필요합니다")
        @Valid
        @Schema(description = "반복 일정 목록")
        private List<RoutineDTO> routine;
    }


    // 온보딩 - 반복 일정 세부
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutineDTO {

        @NotBlank(message = "제목은 필수 입력입니다")
        @Schema(description = "제목", example = "매주 산책")
        private String title;

        @Schema(description = "상세 내용", example = "한강에서 산책")
        private String description;

        @NotNull(message = "요일은 필수 입력입니다")
        @Schema(description = "반복 요일", example = "WEDNESDAY",
                allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
        private Weekday weekday;

        @NotNull(message = "시작 시간은 필수 입력입니다")
        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "시작 시간 (HH:mm 형식)", example = "13:00")
        private LocalTime startTime;


        @NotNull(message = "종료 시간은 필수 입력입니다")
        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "종료 시간 (HH:mm 형식)", example = "00:00")
        private LocalTime endTime;
    }
}
