package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
}
