package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.user.entity.Routine;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
}
