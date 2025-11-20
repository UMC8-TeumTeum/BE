package umc.teumteum.server.domain.teum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.teum.entity.enums.RequestStatus;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teum_request")
public class TeumRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_request_id")
    private TeumRequest parentRequest;

    @Column(name = "graphic_id", nullable = false)
    private Long graphicId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.ACTIVE;


    /*
        양방향 연관관계
    */
    @OneToMany(mappedBy = "parentRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TeumRequest> childRequests;

    @Builder.Default
    @OneToMany(mappedBy = "teumRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TeumResponse> teumResponses = new ArrayList<>();

    @OneToMany(mappedBy = "teumRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    public void markAsClosed() {
        this.status = RequestStatus.CLOSED;
    }

    public void markAsCanceled() { this.status = RequestStatus.CANCELED; }
}
