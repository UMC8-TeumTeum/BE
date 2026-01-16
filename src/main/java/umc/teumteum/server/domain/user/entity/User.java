package umc.teumteum.server.domain.user.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserRole;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"user\"")
public class User extends BaseEntity {

    public static final String DEFAULT_PROFILE_IMAGE = "default.svg";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "social_id", nullable = false, unique = true)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false)
    private SocialType socialType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.ROLE_USER;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "sleep_time")
    private LocalTime sleepTime;

    @Column(name = "wake_time")
    private LocalTime wakeTime;

    @Column(name = "job")
    private String job;

    @Builder.Default
    @Column(name = "profile_image_name")
    private String profileImageName = DEFAULT_PROFILE_IMAGE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "step", nullable = false)
    private UserStep step = UserStep.AGREEMENT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "inactive_at")
    private LocalDateTime inactiveAt;

    @Builder.Default
    @Column(name = "time_public")
    private Boolean timePublic = true;


    /*
        양방향 연관관계
    */
    // 요일별 반복 일정
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Routine> routines = new ArrayList<>();

    // 리마인드 알림
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RemindAlarm> remindAlarms = new ArrayList<>();

    // 알림
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notification> notifications;

    // 내가 팔로우하는 사람들
    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Friend> followings = new ArrayList<>();

    // 나를 팔로우하는 사람들
    @OneToMany(mappedBy = "following", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Friend> followers = new ArrayList<>();

    // 스케줄
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    // 틈 요청
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TeumRequest> teumRequests = new ArrayList<>();

    // 내가 받은 틈 응답
    @OneToMany(mappedBy = "receiverUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TeumResponse> receivedTeumResponses;

    // 위시
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Wish> wishes;


    public void updateStep(UserStep step) {
        this.step = step;
    }

    public void updateNicknameAndJob(String nickname, String jobField) {
        this.nickname = nickname;
        this.job = jobField;
    }

    public void updateSleepPattern(LocalTime sleepTime, LocalTime wakeTime) {
        this.sleepTime = sleepTime;
        this.wakeTime = wakeTime;
    }

    public void updateProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
    }

    public void updateProfile(String nickname, String jobField, Boolean timePublic) {
        this.nickname = nickname;
        this.job = jobField;
        this.timePublic = timePublic;
    }

    // 회원 탈퇴
    public void withdraw(){
        this.status = UserStatus.INACTIVE;
        this.inactiveAt = LocalDateTime.now();

        String token = UUID.randomUUID().toString();

        this.socialId = "deleted-" + this.id + "-" + token;
        this.email = "deleted-" + this.id + "-" + token + "@deleted.local";

        this.nickname = null;
        this.sleepTime = null;
        this.wakeTime = null;
        this.job = null;
        this.timePublic = false;

        this.profileImageName = DEFAULT_PROFILE_IMAGE;
    }
}
