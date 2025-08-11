package umc.teumteum.server.domain.teum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;

import java.time.LocalDate;
import java.util.List;

public class TeumResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeumParticipant {

        @Schema(description = "수신자 ID", example = "1")
        private Long userId;

        @Schema(description = "수신자 닉네임", example = "틈틈")
        private String nickname;

        @Schema(description = "수신자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "TeumTimeSlot : 가능한 시간 구간")
    public static class TeumTimeSlot {

        @Schema(description = "시작 시간", example = "14:00")
        private String start;

        @Schema(description = "종료 시간", example = "15:00")
        private String end;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "TeumAvailableTime : 공통 가능 시간 응답")
    public static class TeumAvailableTime {

        @Schema(description = "날짜", example = "2025-07-14")
        private String date;

        @Schema(description = "공통 가능한 시간대 목록")
        private List<TeumTimeSlot> availableTime;

    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "ScheduledTeumCancel : 틈 나가기 응답")
    public static class ScheduledTeumCancel {

        @Schema(description = "취소 사용자 아이디 목록", example = "[1, 2]")
        private List<Long> cancelledUserIds;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "ScheduledTeumDetail : 약속된 틈 상세 응답")
    public static class ScheduledTeumDetail {

        @Schema(description = "틈 ID", example = "1")
        private Long teumId;

        @Schema(description = "제목", example = "운동하기")
        private String title;

        @Schema(description = "날짜", example = "2025-08-01")
        private String date;

        @Schema(description = "시작 시간", example = "14:00")
        private String startTime;

        @Schema(description = "종료 시간", example = "15:00")
        private String endTime;

        @Schema(description = "스케줄 상태", example = "ACTIVE")
        private ScheduleStatus status;

        @Schema(description = "참여자 목록")
        private List<TeumParticipant> participants;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "ScheduledTeum : 약속된 틈 응답")
    public static class ScheduledTeum {

        @Schema(description = "틈 ID", example = "1")
        private Long teumId;

        @Schema(description = "틈 제목", example = "운동하기")
        private String title;

        @Schema(description = "날짜", example = "2025-08-02")
        private String date;

        @Schema(description = "시간대")
        private List<TeumTimeSlot> time;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "SharedTeumList : 함께한 틈 요청 리스트 응답")
    public static class SharedTeumList {

        @Schema(description = "제목", example = "운동하기")
        private String title;

        @Schema(description = "설명", example = "")
        private String description;

        @Schema(description = "요청 날짜", example = "2025-07-15")
        private String date;

        @Schema(description = "요청 시간 슬롯")
        private TeumTimeSlot time;

        @Schema(description = "요청자 정보")
        private TeumParticipant sender;

        @JsonProperty("isSender")
        @Schema(description = "요청자가 나인지 여부", example = "true")
        private boolean isSender;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "SharedTeumTime : 함께한 틈 시간 응답")
    public static class SharedTeumTime {

        @Schema(description = "함께한 일 수", example = "1")
        private long days;

        @Schema(description = "함께한 시간 수", example = "3")
        private long hours;

        @Schema(description = "함께한 분 수", example = "40")
        private long minutes;

        @Schema(description = "총 시간 (분 단위)", example = "220")
        private long totalMinutes;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "TeumReceived : 내가 받은 틈 요청 응답")
    public static class TeumReceived {

        @Schema(description = "응답 ID", example = "1")
        private Long responseId;

        @Schema(description = "요청 ID", example = "1")
        private Long requestId;

        @Schema(description = "요청 제목", example = "운동 같이 하실 분!")
        private String title;

        @Schema(description = "요청 설명", example = "근처 헬스장에서 같이 운동하자")
        private String description;

        @Schema(description = "그래픽 ID", example = "1")
        private Long graphicId;

        @Schema(description = "읽음 여부", example = "false")
        private boolean isRead;

        @Schema(description = "보낸 사용자 정보")
        private TeumParticipant senderUser;

        @Schema(description = "수신자 총 인원 수 (본인 포함)", example = "1")
        private int receiverCount;

        @Schema(description = "요청 날짜", example = "2025-07-15")
        private String date;

        @Schema(description = "요청 시간 구간")
        private TeumTimeSlot timeSlot;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "TeumRequestDetail : 특정 날짜의 틈 요청 상세 정보")
    public class TeumRequestDetail {

        @Schema(description = "요청 ID", example = "1")
        private Long requestId;

        @Schema(description = "요청 제목", example = "운동 같이 하실 분!")
        private String title;

        @Schema(description = "요청 설명", example = "근처 헬스장에서 같이 운동하자")
        private String description;

        @Schema(description = "요청 날짜", example = "2025-08-04")
        private LocalDate date;

        @Schema(description = "시간 정보 (시작/종료)", implementation = TeumTimeSlot.class)
        private TeumTimeSlot timeSlot;

        @Schema(description = "요청자 정보", implementation = TeumParticipant.class)
        private TeumParticipant requester;

        @JsonProperty("isResend")
        @Schema(description = "재요청 여부", example = "false")
        private boolean isResend;

        @JsonProperty("isCancelled")
        @Schema(description = "취소 여부", example = "true")
        private boolean isCancelled;

        @Schema(description = "PENDING 상태인 응답자 목록")
        private List<TeumParticipant> pending;

        @Schema(description = "ACCEPTED 상태인 응답자 목록")
        private List<TeumParticipant> accepted;

        @Schema(description = "CANCELLED(REJECTED/LEFT) 상태인 응답자 목록")
        private List<TeumParticipant> cancelled;

        @Schema(description = "RESEND 상태인 응답자 목록")
        private List<TeumParticipant> resend;

    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(title = "TeumStatusUpdate : 응답 상태 변경 결과 DTO")
    public class TeumStatusUpdate {

        @Schema(description = "응답 상태", example = "ACCEPTED")
        private ResponseStatus status;

        @Schema(description = "새로운 틈이 생성되었는지 여부 (accepted일 때만 반환)", example = "true")
        private Boolean teumCreated;

        @Schema(description = "생성된 틈 ID (accepted일 때만 반환)", example = "1")
        private Long teumId;
    }


}
