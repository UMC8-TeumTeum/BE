package umc.teumteum.server.domain.home.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.global.util.DateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "TodoInfoResponseDTO : 투두 정보 조회 응답 DTO")
public class TodoInfoResponseDto {

    @Schema(description = "투두 타입", example = "TODO | WISH | TEUM | AI")
    private ScheduleType type;

    @Schema(description = "투두 제목", example = "string")
    private String title;

    @Schema(description = "시작 시간", example = "2025-07-24T10:00")
    private LocalDateTime startTime;

    @Schema(description = "종료 시간", example = "2025-07-24T10:00")
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime endTime;

    @Schema(description = "상세 설명", example = "string")
    private String description;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "빈틈 시간 포함 여부", example = "false")
    private Boolean includeTeum;

    @Schema(description = " 온보딩 리마인드 알림 목록", example = "[1, 30]")
    private List<Integer> onboardingReminder;

    @Schema(description = "리마인드 알림 목록", example = "[1, 3, 5, 10, 30]")
    private List<Integer> remindAlarm;

    @Schema(description = "유저 프로필", example = "[\"string\",\"string\"]")
    private List<String> profileUrl;
}