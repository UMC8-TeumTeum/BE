package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(title = "WishAssignRequestDTO : 위시 투두 등록 요청 DTO")
public class WishAssignRequestDTO {

    @NotNull
    @Schema(description = "시작 시간", example = "2025-07-24T10:00")
    private LocalDateTime startTime;

    @NotNull
    @Schema(description = "종료 시간", example = "2025-07-24T11:00")
    private LocalDateTime endTime;

    @NotNull
    @Schema(description = "강제 등록 여부", example = "false")
    private Boolean isForce;
}
