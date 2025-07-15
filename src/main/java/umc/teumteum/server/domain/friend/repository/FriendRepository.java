package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.friend.entity.Friend;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByFollowerId(Long followerId);

    List<Friend> findByFollowingId(Long id);
}