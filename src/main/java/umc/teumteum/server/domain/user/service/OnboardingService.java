package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.User;

public interface OnboardingService {
    void saveAgreement(OnboardingRequestDto.AgreeRequest request, User user);

    void saveNicknameAndJob(OnboardingRequestDto.NicknameJobRequest request, User user);

    void saveSleepPattern(OnboardingRequestDto.SleepPatternRequest request, User user);
}
