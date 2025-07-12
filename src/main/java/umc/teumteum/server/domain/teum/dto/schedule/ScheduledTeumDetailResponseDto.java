package umc.teumteum.server.domain.teum.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ScheduledTeumDetailResponseDto : 약속된 틈 상세 응답 DTO")
public class ScheduledTeumDetailResponseDto {

    @Schema(description = "틈 ID", example = "1")
    private Long teumId;

    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "날짜", example = "YYYY-MM-DD")
    private String date;

    @Schema(description = "시작 시간", example = "00:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "00:00")
    private String endTime;

    @Schema(description = "참여자 목록")
    private List<ParticipantDto> participants;

    @Schema(description = "이미 지난 틈 여부", example = "false")
    private boolean isPast;
}
