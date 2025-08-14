package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeumStatusScheduler {

    private final ScheduleRepository scheduleRepository;

    @Transactional
    @Scheduled(fixedRate = 300000)
    public void completeExpiredTeumSchedules() {
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> targets = scheduleRepository.findAllExpiredActiveTeums(
                now,
                ScheduleType.TEUM,
                ScheduleStatus.ACTIVE
        );
        if (targets.isEmpty()) return;

        targets.forEach(s -> {
            s.complete();
            log.info("Schedule {} marked as COMPLETED", s.getId());
        });

        scheduleRepository.saveAll(targets);
    }
}
