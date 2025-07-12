package umc.teumteum.server.domain.teum.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "AvailableTimeResponseDto : 공통 가능 시간 응답 DTO")
public class AvailableTimeResponseDto {

    @Schema(description = "날짜", example = "2025-07-14")
    private String date;

    @Schema(description = "공통 가능한 시간대 목록")
    private List<TimeSlot> availableTime;

}
