package umc.teumteum.server.domain.friend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.friend.dto.MutualFriendProjection;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f " +
            "WHERE f.follower.id = :followerId " +
            "AND f.following.status = :status ")
    Slice<Friend> findByFollowerId(@Param("followerId") Long followerId, @Param("status") UserStatus status, Pageable pageable);

    @Query("SELECT f FROM Friend f " +
            "WHERE f.following.id = :followingId " +
            "AND f.follower.status = :status ")
    Slice<Friend> findByFollowingId(@Param("followingId") Long followingId, @Param("status") UserStatus status, Pageable pageable);

    Optional<Friend> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerAndFollowing(User loginUser, User targetUser);

    Optional<Friend> findByFollowerAndFollowing(User loginUser, User targetUser);

    @Query("""
        SELECT new umc.teumteum.server.domain.friend.dto.MutualFriendProjection(
            u.id,
            u.nickname,
            u.profileImageName
        )
        FROM Friend f1
        JOIN f1.following u
        JOIN Friend f2 ON f2.follower = u AND f2.following = :user
        WHERE f1.follower = :user
          AND u.status = :status
          AND u.id <> :excludeUserId
        ORDER BY u.nickname ASC
        """)
    Slice<MutualFriendProjection> findMutualFriendsExcluding(
            @Param("user") User user,
            @Param("excludeUserId") Long excludeUserId,
            @Param("status") UserStatus status,
            Pageable pageable
    );

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