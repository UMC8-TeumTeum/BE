package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(title = "TeumResendRequestDto : 재요청(시간 제안) 생성 DTO")
public class TeumResendRequestDto {

    @Schema(description = "시작 시간", example = "00:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "00:00")
    private String endTime;
}
