package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 필요시 커스텀 메서드 추가
}