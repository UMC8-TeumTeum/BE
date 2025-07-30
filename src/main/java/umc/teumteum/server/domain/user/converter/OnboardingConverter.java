package umc.teumteum.server.domain.user.converter;

import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;

public class OnboardingConverter {

    public static OnboardingResponseDto.ProfileImagePresignedUrlResponse toProfileImagePresignedUrlResponse(
            String presignedUrl,
            String fileName
    ) {
        return OnboardingResponseDto.ProfileImagePresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .fileName(fileName)
                .build()
                ;
    }
}
