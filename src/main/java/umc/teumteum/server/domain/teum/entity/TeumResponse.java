package umc.teumteum.server.domain.teum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teum_response")
public class TeumResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private TeumRequest teumRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiverUser;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResponseStatus status = ResponseStatus.PENDING;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 응답 상태 변경
     */
    public void changeStatus(ResponseStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * 응답 읽음 처리
     */
    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }
}
