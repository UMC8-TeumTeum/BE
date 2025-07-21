package umc.teumteum.server.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

  // 투두
  DAILY_TODO("오늘의 투두를 알려드려요", "오늘의 투두가 도착했어요!", RelatedEntityType.NONE),             // 9시에 보내는 투두 알림

  // 친구
  TEUM_REQUEST("친구와의 요청에 새로운 소식이 있어요", "새롭게 온 요청이 있어요!", RelatedEntityType.TEUM_REQUEST),// 틈 요청이 새롭게 왔을 때
  TEUM_ACCEPTED("친구와의 요청에 새로운 소식이 있어요", "친구가 요청을 수락했어요!", RelatedEntityType.TEUM_RESPONSE),// 친구에게 보낸 틈 요청이 수락
  TEUM_DECLINED("친구와의 요청에 새로운 소식이 있어요", "친구가 요청을 거절했어요!", RelatedEntityType.TEUM_RESPONSE),// 친구에게 보낸 틈 요청이 거절
  TEUM_SUGGESTED("친구와의 요청에 새로운 소식이 있어요", "친구가 새로운 시간을 제안했어요!", RelatedEntityType.TEUM_RESPONSE),// 친구가 시간을 새롭게 제안
  TEUM_CANCELED("친구와의 요청에 새로운 소식이 있어요", "친구와의 약속이 취소됐어요!", RelatedEntityType.SCHEDULE),// 친구가 약속된 틈 취소

  // 팔로워
  FOLLOW("새로운 팔로워가 있어요", "새로운 팔로워가 생겼어요!", RelatedEntityType.FRIEND);;

  private final String title;//FCM 푸시용 제목
  private final String content;// 화면쪽에서 보여줄 문구
  private final RelatedEntityType relatedEntityType;

  public boolean isStorable() {
    return this != DAILY_TODO;
  }

  public boolean isPushOnly() {
    return !isStorable();
  }

}
