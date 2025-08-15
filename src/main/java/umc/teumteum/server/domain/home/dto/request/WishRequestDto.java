package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;

import java.time.LocalDateTime;
import java.util.List;

public class WishRequestDto {

    @Getter
    @NoArgsConstructor
    @Schema(title = "WisDto : 위시 등록 Dto")
    public static class CreateDto{
        @NotBlank
        @Schema(description = "위시 제목", example = "string")
        private String title;
        @Schema(description = "상세 설명", example = "string")
        private String content;
        @NotNull
        @Schema(description = "예상 소요 시간", example = "10m")
        private EstimatedDuration estimatedDuration;
        @NotEmpty
        @Schema(description = "위시 카테고리", example = "[1]")
        private List<Long> categories;
    }

    @Getter
    @NoArgsConstructor
    @Schema(title = "WishDeleteDto : 위시 삭제 요청 Dto")
    public static class WishDeleteDto{
        @NotEmpty
        @NotNull
        @Schema(description = "위시 ID 리스트", example = "[1,2,3]")
        private List<Long> wishIds;
    }

    @Getter
    @NoArgsConstructor
    @Schema(title = "WishAssignDto : 위시 투두 등록 요청 Dto")
    public static class WishAssignDto{
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
}
