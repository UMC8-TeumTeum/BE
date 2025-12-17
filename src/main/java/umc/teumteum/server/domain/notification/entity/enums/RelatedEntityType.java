package umc.teumteum.server.domain.notification.entity.enums;

public enum RelatedEntityType {
  NONE,           // DAILY_TODO 등등
  TEUM_REQUEST,   // 틈 요청
  TEUM_RESPONSE,  // 틈 응답(틈 수락, 틈 거절, 틈 제악)
  SCHEDULE,       // 틈 취소(틈이 수락되었다가 취소되었을 때)
  FRIEND ,        // 팔로우 팔로잉 등
  REMINDER        // 리마인드 알림
}
