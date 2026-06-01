package umc.teumteum.server.domain.user.service;

import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.User;

public interface OnboardingLockService {
    void saveRoutinesWithLock(OnboardingRequestDto.RoutineListRequest request, User user);
}
