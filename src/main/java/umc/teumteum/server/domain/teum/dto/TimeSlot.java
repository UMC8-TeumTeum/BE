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
@Schema(title = "TimeSlot : 가능한 시간 구간")
public class TimeSlot {

    @Schema(description = "시작 시간", example = "00:00")
    private String start;

    @Schema(description = "종료 시간", example = "00:00")
    private String end;
}