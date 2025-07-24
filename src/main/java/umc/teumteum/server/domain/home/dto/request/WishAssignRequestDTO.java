package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "WishAssignRequestDTO : 위시 투두 등록 요청 DTO")
public class WishAssignRequestDTO {

    @NotNull
    @Schema(description = "날짜", example = "2025-07-24")
    private LocalDate date;

    @NotNull
    @Schema(description = "시작시간", example = "09:00")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "종료시간", example = "10:00")
    private LocalTime endTime;

    @NotNull
    @Schema(description = "강제 등록 여부", example = "false")
    private Boolean isForce;
}
