package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ScheduledTeumExitResponseDto : 틈 나가기 응답 DTO")
public class ScheduledTeumExitResponseDto {

    @Schema(description = "남은 참여자 수", example = "2")
    private int remainingMembers;

    @Schema(description = "변경된 틈 상태", example = "active")
    private String teumStatus; // active or cancelled
}
