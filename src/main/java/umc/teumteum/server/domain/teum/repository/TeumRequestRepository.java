package umc.teumteum.server.domain.teum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;

public interface TeumRequestRepository extends JpaRepository<TeumRequest, Long> {
    // 필요 시 커스텀 메서드 추가
}