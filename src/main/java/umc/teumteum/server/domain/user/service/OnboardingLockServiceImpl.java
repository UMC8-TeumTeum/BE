package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.RedissonLockUtil;

@Service
@RequiredArgsConstructor
public class OnboardingLockServiceImpl implements OnboardingLockService {

    private final RedissonLockUtil redissonLockUtil;
    private final OnboardingService onboardingService;

    // 반복일정 등록 요청 시 Redisson 락을 걸고 saveRoutines 로직 실행
    @Override
    public void saveRoutinesWithLock(OnboardingRequestDto.RoutineListRequest request, User user) {
        String lockKey = createLockKey(user.getId());
        redissonLockUtil.executeWithLockNoWait(lockKey,
                () -> onboardingService.saveRoutines(request, user)
        );
    }

    private String createLockKey(Long userId) {
        return String.format("ONBOARDING_ROUTINES_LOCK:%d", userId);
    }
}
