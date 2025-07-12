package umc.teumteum.server.domain.teum.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDto {

    @Schema(description = "수신자 ID", example = "1")
    private Long userId;

    @Schema(description = "수신자 닉네임", example = "string")
    private String nickname;

    @Schema(description = "수신자 프로필 이미지 URL", example = "string")
    private String profileImageUrl;
}