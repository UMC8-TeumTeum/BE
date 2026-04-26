package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeumRequestStatusScheduler {

    private final TeumRequestRepository teumRequestRepository;

    @Transactional
    @Scheduled(cron = "0 0/10 * * * *")
    public void updateClosedTeums() {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        List<TeumRequest> closed = teumRequestRepository.findAllClosed(today, nowTime);

        if (closed.isEmpty()) return;

        closed.forEach(request -> {
            request.markAsClosed();
            log.info("TeumRequest {} marked as closed", request.getId());
        });

        teumRequestRepository.saveAll(closed);
    }
}
