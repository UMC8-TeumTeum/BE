package umc.teumteum.server.domain.report.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.report.entity.enums.ReportReasonType;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report_reason")
public class ReportReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false)
    private ReportReasonType type;

    public boolean isOther() {
        return this.type == ReportReasonType.OTHER;
    }
}
