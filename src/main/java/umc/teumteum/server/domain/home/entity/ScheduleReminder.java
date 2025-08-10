package umc.teumteum.server.domain.home.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule_reminder")
public class ScheduleReminder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "reminder_time", nullable = false)
    private Integer reminderTime;

    @Column(name = "alarm_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlarmStatus alarmStatus = AlarmStatus.INACTIVE;

    /**
     *  필드 변경 메소드
     */
    public void updateStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
    }
}
