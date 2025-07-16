package umc.teumteum.server.domain.teum.dto.teum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Schema(description = "재요청을 보내는 유저 (원래 요청의 수신자)", example = "1")
    private Long senderUserId;
}
