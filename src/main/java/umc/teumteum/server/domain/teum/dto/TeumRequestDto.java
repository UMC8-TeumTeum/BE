package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "TeumRequestDto : 틈 요청 생성 DTO")
public class TeumRequestDto {

    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "상세 내용", example = "string")
    private String description;

    @Schema(description = "날짜 (YYYY-MM-DD)", example = "YYYY-MM-DD")
    private String date;

    @Schema(description = "시작 시간 (HH:MM)", example = "00:00")
    private String startTime;

    @Schema(description = "종료 시간 (HH:MM)", example = "00:00")
    private String endTime;

    @Schema(description = "그래픽 ID", example = "1")
    private Long graphicId;

    @Schema(description = "수신자 ID 배열", example = "[0, 1]")
    private List<Long> receiverUserIds;

    @Schema(description = "재요청 시 기존 요청 ID (없으면 null)", example = "null")
    private Long parentRequestId;
}
