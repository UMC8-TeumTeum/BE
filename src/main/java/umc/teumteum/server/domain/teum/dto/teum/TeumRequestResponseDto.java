package umc.teumteum.server.domain.teum.dto.teum;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TeumRequestResponseDto : 특정 날짜의 틈 요청 상세 정보")
public class TeumRequestResponseDto {

    @Schema(description = "요청 ID", example = "1")
    private Long requestId;

    @Schema(description = "요청 제목", example = "운동 같이 하실 분!")
    private String title;

    @Schema(description = "요청 설명", example = "근처 헬스장에서 같이 운동하실 분 구해요.")
    private String description;

    @Schema(description = "요청 날짜", example = "2025-08-04")
    private LocalDate date;

    @Schema(description = "시간 정보 (시작/종료)", implementation = TimeSlot.class)
    private TimeSlot timeSlot;

    @Schema(description = "요청자 정보", implementation = ParticipantDto.class)
    private ParticipantDto requester;

    @JsonProperty("isResend")
    @Schema(description = "재요청 여부", example = "false")
    private boolean isResend;

    @JsonProperty("isCancelled")
    @Schema(description = "취소 여부", example = "true")
    private boolean isCancelled;

    @Schema(description = "PENDING 상태인 응답자 목록")
    private List<ParticipantDto> pending;

    @Schema(description = "ACCEPTED 상태인 응답자 목록")
    private List<ParticipantDto> accepted;

    @Schema(description = "CANCELLED(REJECTED/LEFT) 상태인 응답자 목록")
    private List<ParticipantDto> cancelled;

    @Schema(description = "RESEND 상태인 응답자 목록")
    private List<ParticipantDto> resend;

}
