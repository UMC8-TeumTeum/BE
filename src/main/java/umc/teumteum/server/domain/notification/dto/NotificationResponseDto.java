package umc.teumteum.server.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


public class NotificationResponseDto {

  @Getter
  @Builder
  @AllArgsConstructor
  public static class SliceResponseDto {
    @Schema(description = "알림 목록")
    private List<NotificationResponseDto.NotificationDto> content;
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;
    @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
    private int currentPage;
    @Schema(description = "페이지당 알림 수", example = "10")
    private int size;

  }
  @Getter
  @Builder
  @AllArgsConstructor
  public static class NotificationDto {
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

    // 약속 날짜
    @Schema(description = "알림이 가리키는 약속/일정 날짜", example = "2025-07-25T18:00:00", nullable = true)
    private LocalDateTime eventDate;

  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class ReadResponseDto {

    @Schema(description = "읽음 처리된 알림 ID", example = "42")
    private Long notificationId;

    @Schema(description = "알림 읽음 여부", example = "true")
    private Boolean isRead;
  }
}