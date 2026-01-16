package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.user.entity.User;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f " +
            "WHERE f.follower.id = :followerId " +
            "AND f.following.status = 'ACTIVE'")
    Slice<Friend> findByFollowerId(@Param("followerId") Long followerId, Pageable pageable);

    @Query("SELECT f FROM Friend f " +
            "WHERE f.following.id = :followingId " +
            "AND f.follower.status = 'ACTIVE'")
    Slice<Friend> findByFollowingId(@Param("followingId") Long followingId, Pageable pageable);

    Optional<Friend> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerAndFollowing(User loginUser, User targetUser);

    Optional<Friend> findByFollowerAndFollowing(User loginUser, User targetUser);

    @Query("SELECT f1 FROM Friend f1 " +
            "WHERE f1.follower = :user " +
            "AND f1.following.status = 'ACTIVE' " +
            "AND EXISTS (SELECT f2 FROM Friend f2 " +
            "WHERE f2.follower = f1.following " +
            "AND f2.following = :user) " +
            "AND f1.following != :excludeUser")
    Slice<Friend> findMutualFriendsExcluding(User user, User excludeUser, Pageable pageable);

    // 차단 시 두 유저 간의 모든 팔로우 관계(A->B, B->A)를 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Friend f " +
            "WHERE (f.follower = :user1 AND f.following = :user2) " +
            "OR (f.follower = :user2 AND f.following = :user1)")
    void deleteFriendshipsBetween(@Param("user1") User user1, @Param("user2") User user2);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Friend f WHERE f.follower.id = :userId OR f.following.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}