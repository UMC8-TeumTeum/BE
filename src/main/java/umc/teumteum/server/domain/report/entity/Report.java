package umc.teumteum.server.domain.report.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import umc.teumteum.server.domain.report.entity.enums.ReportStatus;
import umc.teumteum.server.domain.report.entity.enums.TargetType;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report")
@Check(constraints =
        "(" +
                // 둘 중 하나만 NOT NULL (XOR)
                " ( (target_user_id IS NOT NULL) <> (target_request_id IS NOT NULL) ) " +
                " AND " +
                // target_type 이 USER 인지 여부와 target_user_id 존재 여부가 같음
                " ( (target_type = 'USER') = (target_user_id IS NOT NULL) ) " +
                ")"
)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = true)
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_request_id", nullable = true)
    private TeumRequest teumRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reason_id", nullable = false)
    private ReportReason reason;

    @Column(name = "other_reason_text", columnDefinition = "TEXT", nullable = true)
    private String otherReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.OPEN;

}
