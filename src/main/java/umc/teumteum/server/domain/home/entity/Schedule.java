package umc.teumteum.server.domain.home.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.dto.TodoRequestDTO;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule")
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ScheduleType type;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Builder.Default
    @Column(name = "include_teum", nullable = false)
    private Boolean includeTeum = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teum_request_id")
    private TeumRequest teumRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;

    /*
        양방향 연관관계
    */
    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ScheduleReminder> scheduleReminders = new ArrayList<>();

    public void updateField(TodoRequestDTO dto) {
        this.title = dto.getTitle();
        this.date = dto.getDate();
        this.startTime = LocalDateTime.of(dto.getDate(), dto.getStartTime());
        this.endTime = LocalDateTime.of(dto.getDate(), dto.getEndTime());
        this.description = dto.getDescription();
        this.isPublic = dto.getIsPublic();
        this.includeTeum = dto.getIncludeTeum();
    }
}
