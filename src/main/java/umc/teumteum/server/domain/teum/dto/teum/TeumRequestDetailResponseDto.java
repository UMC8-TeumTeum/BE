package umc.teumteum.server.domain.teum.dto.teum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TeumRequestDetailResponseDto : 틈 요청 상세 응답 DTO")
public class TeumRequestDetailResponseDto {

    @Schema(description = "요청 ID", example = "1")
    private Long requestId;

    @Schema(description = "보낸 사용자 정보")
    private ParticipantDto senderUser;

    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "내용", example = "string")
    private String description;

    @Schema(description = "날짜", example = "YYYY-MM-DD")
    private String date;

    @Schema(description = "시작 시간", example = "00:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "00:00")
    private String endTime;

    @Schema(description = "그래픽 ID", example = "1")
    private int graphicId;

    @Schema(description = "읽음 여부", example = "true")
    private boolean isRead;
}
