package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TeumReceivedResponseDto : 내가 받은 틈 요청 응답 DTO")
public class TeumReceivedResponseDto {

    @Schema(description = "응답 ID", example = "1")
    private Long responseId;

    @Schema(description = "요청 ID", example = "1")
    private Long requestId;

    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;

    @Schema(description = "보낸 사람 닉네임", example = "string")
    private String senderNickname;

    @Schema(description = "보낸 사람 프로필 이미지 URL", example = "string")
    private String senderProfileImageUrl;

    @Schema(description = "수신자 총 인원 수 (본인 포함)", example = "1")
    private int receiverCount;
}
