package umc.teumteum.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_setting")
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "today_todo", nullable = false)
    private Boolean todayTodo = true;

    @Builder.Default
    @Column(name = "remind_alarm", nullable = false)
    private Boolean remindAlarm = true;

    @Builder.Default
    @Column(name = "receive_request", nullable = false)
    private Boolean receiveRequest = true;

    @Builder.Default
    @Column(name = "accept_request", nullable = false)
    private Boolean acceptRequest = true;

    @Builder.Default
    @Column(name = "reject_request", nullable = false)
    private Boolean rejectRequest = true;

    @Builder.Default
    @Column(name = "change_time", nullable = false)
    private Boolean changeTime = true;

    @Builder.Default
    @Column(name = "is_canceled", nullable = false)
    private Boolean isCanceled = true;
}