package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.user.entity.User;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Slice<Friend> findByFollowerId(Long followerId, Pageable pageable);

    Slice<Friend> findByFollowingId(Long followingId, Pageable pageable);

    Optional<Friend> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerAndFollowing(User loginUser, User targetUser);

    Optional<Friend> findByFollowerAndFollowing(User loginUser, User targetUser);

    @Query("SELECT f1 FROM Friend f1 " +
            "WHERE f1.follower = :user " +
            "AND EXISTS (SELECT f2 FROM Friend f2 " +
            "WHERE f2.follower = f1.following " +
            "AND f2.following = :user) " +
            "AND f1.following != :excludeUser")
    Slice<Friend> findMutualFriendsExcluding(User user, User excludeUser, Pageable pageable);
}