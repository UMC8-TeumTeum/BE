package umc.teumteum.server.domain.teum.dto.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(title = "SharedTeumListResponseDto : 함께한 틈 요청 리스트 응답 DTO")
public class SharedTeumListResponseDto {

    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "설명", example = "string")
    private String description;

    @Schema(description = "요청 날짜", example = "2025-07-15")
    private String date;

    @Schema(description = "요청 시간 슬롯")
    private TimeSlot time;

    @Schema(description = "요청자 정보")
    private ParticipantDto sender;

    @JsonProperty("isSender")
    @Schema(description = "요청자가 나인지 여부", example = "true")
    private boolean isSender;
}
