package umc.teumteum.server.domain.home.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "TeumResponse : 빈틈시간 조회 응답 DTO")
public class TodayScheduleResponseDTO {

    @Schema(description = "시작 시간", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Schema(description = "타입", example = "TODO | SLEEP | EMPTY")
    private String type;

}
