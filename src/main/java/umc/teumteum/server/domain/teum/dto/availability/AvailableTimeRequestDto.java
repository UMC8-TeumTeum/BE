package umc.teumteum.server.domain.teum.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "AvailableTimeRequestDto : 공통 가능 시간 요청 DTO")
public class AvailableTimeRequestDto {

    @Schema(description = "참여자 ID 목록 (요청자는 자동 포함됨)", example = "[1, 2, 3]")
    private List<Long> userIds;

    @Schema(description = "날짜 (YYYY-MM-DD)", example = "2025-07-30")
    private String date;
}