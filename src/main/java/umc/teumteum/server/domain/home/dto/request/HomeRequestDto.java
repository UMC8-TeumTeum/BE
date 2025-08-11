package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;


public class HomeRequestDto {

    @Getter
    @NoArgsConstructor
    public static class AlarmDto {
        @Schema(description = "todoId" , example = "1")
        private Long todoId;

        @Schema(description = "알림 상태", example = "ACTIVE")
        private AlarmStatus alarmStatus;
    }

}
