package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OnboardingResponseDto {

    // 온보딩 - 프로필 이미지 업로드용 URl 발급
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileImagePresignedUrlResponse {

        @Schema(description = "S3에 업로드할 수 있는 Presigned URL")
        private String presignedUrl;

        @Schema(description = "DB에 저장할 파일 이름 (UUID.확장자)")
        private String fileName;
    }
}
