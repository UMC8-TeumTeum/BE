package umc.teumteum.server.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.entity.User;

public interface OnboardingService {
    void saveAgreements(OnboardingRequestDto.AgreeRequest request, User user);

    void saveNicknameAndJob(OnboardingRequestDto.NicknameJobRequest request, User user);

    void saveSleepPattern(OnboardingRequestDto.SleepPatternRequest request, User user);

    void saveRoutines(OnboardingRequestDto.RoutineListRequest request, User user);

    OnboardingResponseDto.ProfileImagePresignedUrlResponse generateProfileImagePresignedUrl(HttpServletRequest httpServletRequest, OnboardingRequestDto.ProfileImagePresignedUrlRequest request, User user);

    void saveProfileImage(HttpServletRequest httpServletRequest, OnboardingRequestDto.ProfileImageRequest request, User user);

    void saveRemindAlarms(OnboardingRequestDto.RemindAlarmList request, User user);
}
