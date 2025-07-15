package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
