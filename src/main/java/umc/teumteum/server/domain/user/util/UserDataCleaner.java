package umc.teumteum.server.domain.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.friend.repository.BlockRepository;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.home.repository.WishCategoryRepository;
import umc.teumteum.server.domain.home.repository.WishRepository;
import umc.teumteum.server.domain.notification.repository.NotificationRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.AgreementRepository;
import umc.teumteum.server.domain.user.repository.NotificationSettingRepository;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.RoutineRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDataCleaner {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;

    private final WishRepository wishRepository;
    private final WishCategoryRepository wishCategoryRepository;
    private final BlockRepository blockRepository;
    private final FriendRepository friendRepository;

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final RoutineRepository routineRepository;
    private final RemindAlarmRepository remindAlarmRepository;
    private final AgreementRepository agreementRepository;
    private final FcmTokenRepository fcmTokenRepository;


    @Transactional
    public void clean(Long userId) {
        // 1. 스케줄 & 스케줄 리마인드
        scheduleReminderRepository.deleteAllByUserId(userId);
        scheduleRepository.deleteAllByUserId(userId);
        // 2. 위시 & 위시카테고리
        wishCategoryRepository.deleteAllByUserId(userId);
        wishRepository.deleteAllByUserId(userId);
        // 3. 차단
        blockRepository.deleteAllByUserId(userId);
        // 4. 친구
        friendRepository.deleteAllByUserId(userId);
        // 5. 알림
        notificationRepository.deleteAllByUserId(userId);
        // 6. 알림 설정
        notificationSettingRepository.deleteAllByUserId(userId);
        // 7. 루틴
        routineRepository.deleteAllByUserId(userId);
        // 8. 리마인드 알림
        remindAlarmRepository.deleteAllByUserId(userId);
        // 9. 동의
        agreementRepository.deleteAllByUserId(userId);
        // 10. fcm 토큰
        fcmTokenRepository.deleteAllByUserId(userId);
    }
}
