package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.UserRequestDTO;
import umc.teumteum.server.domain.user.entity.User;

public interface OnboardingService {
    void saveAgreement(UserRequestDTO.AgreeRequest request, User user);

    void saveNicknameAndJob(UserRequestDTO.NicknameJobRequest request, User user);

    void saveSleepPattern(UserRequestDTO.SleepPatternRequest request, User user);
}
