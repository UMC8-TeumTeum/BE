package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.User;

import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    Optional<Agreement> findByUser(User user);

    boolean existsByUser(User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Agreement a WHERE a.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}