package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.friend.entity.Friend;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Slice<Friend> findByFollowerId(Long followerId, Pageable pageable);

    Slice<Friend> findByFollowingId(Long followingId, Pageable pageable);

    Optional<Friend> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

}