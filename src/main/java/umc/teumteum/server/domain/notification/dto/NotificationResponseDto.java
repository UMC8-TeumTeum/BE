package umc.teumteum.server.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 알림 응답 DTO")
public class NotificationResponseDto {

  @Schema(description = "알림 ID", example = "42")
  private Long id;

  @Schema(description = "알림 타입 (TEUM_REQUEST, FOLLOW 등)", example = "TEUM_REQUEST")
  private String type;

  @Schema(description = "요청,응답,친구 Id", example = "2")
  private Long relatedId;

  @Schema(description = "알림 내용", example = "친구와의 요청에 새로운 소식이 있어요")
  private String content;

  @Schema(description = "알림 읽음 여부", example = "false")
  private Boolean isRead;

  @Schema(description = "알림 생성 시각", example = "2025-07-21T09:00:00")
  private LocalDateTime createdAt;

  // 친구 정보
  @Schema(description = "관련된 친구 ID", example = "7", nullable = true)
  private Long friendId;

  @Schema(description = "관련된 친구 닉네임", example = "고냐니", nullable = true)
  private String friendNickname;

  @Schema(description = "친구 프로필 이미지 Presigned URL", example = "https://teumteum~~", nullable = true)
  private String firendProfileImage;
}