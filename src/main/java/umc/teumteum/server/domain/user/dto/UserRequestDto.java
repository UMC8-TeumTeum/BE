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
}
