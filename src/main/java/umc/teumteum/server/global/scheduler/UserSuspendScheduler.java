package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSuspendScheduler {

    private final UserRepository userRepository;

    /**
     * 매일 자정(00:00)에 정지 기간이 만료된 유저들을 활성화
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void activateExpiredUsersAtMidnight() {
        LocalDateTime now = LocalDateTime.now();

        // 정지 상태이고, 정지 기한(suspendedUntil)이 현재 시점보다 이전인 유저 조회
        List<User> expiredUsers = userRepository.findAllByStatusAndSuspendedUntilBefore(
                UserStatus.SUSPENDED, now
        );

        // 일괄 활성화
        expiredUsers.forEach(User::activate);
    }
}