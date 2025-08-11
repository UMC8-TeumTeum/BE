package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "TodoRequestDTO : 투두 등록/수정 DTO")
public class TodoRequestDto {

    @NotBlank
    @Schema(description = "투두 제목", example = "자료구조 공부하기")
    private String title;

    @NotNull
    @Schema(description = "시작 시간", example = "2025-07-24T10:00")
    private LocalDateTime startTime;

    @NotNull
    @Schema(description = "종료 시간", example = "2025-07-24T11:00")
    private LocalDateTime endTime;

    @Schema(description = "상세 설명", example = "linked list 공부하기")
    private String description;

    @NotNull
    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @NotNull
    @Schema(description = "빈틈 시간 포함 여부", example = "false")
    private Boolean includeTeum;

    @Schema(description = "리마인드 알림 목록", example = "[{ \"alarm\": 10, \"status\": \"ACTIVE\" }]")
    private List<ReminderAlarmDto> remindAlarm;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReminderAlarmDto {
        private Integer alarm;
        private AlarmStatus status;
    }
}