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
    @Column(name = "teum", nullable = false)
    private Boolean teum = true;

    @Builder.Default
    @Column(name = "follow", nullable = false)
    private Boolean follow = true;

    public void update(Boolean todayTodo, Boolean remindAlarm, Boolean follow, Boolean teum) {
        this.todayTodo = todayTodo;
        this.remindAlarm = remindAlarm;
        this.follow = follow;
        this.teum = teum;
    }
}