package umc.teumteum.server.domain.teum.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SharedTeumListResponseDto : 공유된 틈 요청 리스트 응답 DTO")
public class SharedTeumListResponseDto {

    @Schema(description = "틈 ID", example = "1")
    private Long teumId;

    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "요청 날짜", example = "2025-07-15")
    private String date;

    @Schema(description = "요청 시간 슬롯")
    private TimeSlot time;

    @Schema(description = "요청자 정보")
    private ParticipantDto sender;

    @Schema(description = "요청자가 나인지 여부", example = "true")
    private boolean isRequester;
}
