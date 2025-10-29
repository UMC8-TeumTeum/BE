package umc.teumteum.server.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "block",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_block_blocker_blocked",
                        columnNames = {"blocker_user_id", "blocked_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_block_blocker", columnList = "blocker_user_id"),
                @Index(name = "idx_block_blocked", columnList = "blocked_user_id")
        }
)
public class Block extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_user_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blocked;

}
