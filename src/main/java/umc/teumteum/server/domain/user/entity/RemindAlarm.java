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
@Table(
        name = "remind_alarm",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_remind_alarm_user_minutes",
                        columnNames = {"user_id", "minutes_before"}
                )
        }
)
public class RemindAlarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "minutes_before", nullable = false)
    private Integer minutesBefore;
}
