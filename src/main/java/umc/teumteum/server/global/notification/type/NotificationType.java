package umc.teumteum.server.global.notification.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

  // 투두
  DAILY_TODO("오늘의 투두를 알려드려요", null),             // 9시에 보내는 투두 알림

  // 친구
  TEUM_REQUEST("친구와의 요청에 새로운 소식이 있어요", null),  // 틈 요청이 새롭게 왔을 때
  TEUM_ACCEPTED("친구와의 요청에 새로운 소식이 있어요", null), // 친구에게 보낸 틈 요청이 수락
  TEUM_DECLINED("친구와의 요청에 새로운 소식이 있어요", null), // 친구에게 보낸 틈 요청이 거절
  TEUM_SUGGESTED("친구와의 요청에 새로운 소식이 있어요", null),// 친구가 시간을 새롭게 제안
  TEUM_CANCELED("친구와의 요청에 새로운 소식이 있어요", null), // 친구가 약속된 틈 취소

  // 팔로워
  FOLLOW("새로운 팔로워가 있어요", null);

  private final String title;
  private final String content;
}
