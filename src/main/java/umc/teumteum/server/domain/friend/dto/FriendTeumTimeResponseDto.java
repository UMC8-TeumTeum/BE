package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "FriendTeumTimeResponse : 친구 빈틈 시간 응답 DTO")
public class FriendTeumTimeResponseDto {

    @Schema(description = "일 단위 빈틈 시간", example = "0")
    private int days;

    @Schema(description = "시간 단위 빈틈 시간", example = "0")
    private int hours;

    @Schema(description = "분 단위 빈틈 시간", example = "0")
    private int minutes;

    @Schema(description = "총 빈틈 시간 (분)", example = "60")
    private long totalMinutes;
}
