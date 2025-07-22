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

public class UserRequestDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreeRequest {

        @NotNull
        @Schema(description = "서비스 이용약관 동의 여부 (필수)", example = "true")
        private Boolean tosConsent;

        @NotNull
        @Schema(description = "개인정보 수집 및 이용 동의 여부 (필수)", example = "true")
        private Boolean privacyConsent;

        @NotNull
        @Schema(description = "개인정보 제3자 제공 동의 여부 (선택)", example = "false")
        private Boolean thirdPartyConsent;

        @NotNull
        @Schema(description = "마케팅 정보 수신 동의 여부 (선택)", example = "false")
        private Boolean marketingConsent;
    }

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
}
