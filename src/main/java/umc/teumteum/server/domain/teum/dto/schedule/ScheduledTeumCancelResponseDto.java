package umc.teumteum.server.domain.teum.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ScheduledTeumCancelResponseDto : 틈 나가기 응답 DTO")
public class ScheduledTeumCancelResponseDto {
    private List<Long> cancelledUserIds;
}