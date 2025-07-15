package umc.teumteum.server.domain.home.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "CreateTodoRequestDTO : 투두 생성 DTO")
public class CreateTodoRequestDTO {

    @Schema(description = "투두 제목", example = "string")
    private String title;

    @Schema(description = "날짜", example = "2025-07-15")
    private LocalDate date;

    @Schema(description = "시작 시간", example = "09:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Schema(description = "상세 설명", example = "string")
    private String description;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "빈틈 시간 포함 여부", example = "false")
    private Boolean includeTeum;

    @Schema(description = "리마인드 알림 목록", example = "[1, 3, 5, 10, 30]")
    private List<Integer> remindAlarm;
}