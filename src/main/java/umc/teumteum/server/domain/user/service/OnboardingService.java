package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.entity.User;

public interface OnboardingService {
    void saveAgreements(OnboardingRequestDto.AgreeRequest request, User user);

    void saveNicknameAndJob(OnboardingRequestDto.NicknameJobRequest request, User user);

    void saveSleepPattern(OnboardingRequestDto.SleepPatternRequest request, User user);

    void saveRoutines(OnboardingRequestDto.RoutineListRequest request, User user);

    OnboardingResponseDto.ProfileImagePresignedUrlResponse generateProfileImagePresignedUrl(OnboardingRequestDto.ProfileImagePresignedUrlRequest request, User user);

    void saveProfileImage(OnboardingRequestDto.ProfileImageRequest request, User user);

    void saveRemindAlarms(OnboardingRequestDto.RemindAlarmList request, User user);
}
