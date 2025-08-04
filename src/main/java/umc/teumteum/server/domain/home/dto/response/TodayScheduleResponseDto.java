package umc.teumteum.server.domain.home.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import umc.teumteum.server.global.util.TimeSerializer;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "TeumResponse : 빈틈시간 조회 응답 DTO")
public class TodayScheduleResponseDto {

    @Schema(description = "시작 시간", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:00")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime endTime;

    @Schema(description = "타입", example = "TODO | SLEEP | EMPTY")
    private String type;

}
