package umc.teumteum.server.domain.notification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

  private Long id;              // 알림 ID
  private String type;          // 알림 타입 (TEUM_REQUEST,,, 등)
  private String content;       // 알림 내용
  private Boolean isRead;       // 읽음 여부
  private LocalDateTime createdAt; // 알림 생성 시각

  //친구 정보
  private Long friendId;             // 요청과 관련된 친구 Id
  private String friendNickname;     // 친구 닉네임
  private String firendProfileImage; // 친구 프로필 이미지
}
