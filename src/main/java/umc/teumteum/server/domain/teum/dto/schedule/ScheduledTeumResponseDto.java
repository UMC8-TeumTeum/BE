package umc.teumteum.server.domain.teum.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ScheduledTeumResponseDto : 약속된 틈 응답 DTO")
public class ScheduledTeumResponseDto {

    @Schema(description = "틈 ID", example = "1")
    private Long teumId;

    @Schema(description = "틈 제목", example = "string")
    private String title;

    @Schema(description = "날짜", example = "YYYY-MM-DD")
    private String date;

    @Schema(description = "시작 시간", example = "00:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "00:00")
    private String endTime;
}
