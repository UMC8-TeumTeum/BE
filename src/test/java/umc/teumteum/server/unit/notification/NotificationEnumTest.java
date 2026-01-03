package umc.teumteum.server.unit.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;
import umc.teumteum.server.domain.notification.entity.enums.RelatedEntityType;
import umc.teumteum.server.support.RedisTestContainerSupport;

@DisplayName("NotificationEnum - Notification enum 관련 단위 테스트")
public class NotificationEnumTest {
  @Test
  @DisplayName("[shouldBeStored / isPushOnly] - TC1 DAILY_TODO는 저장 안되고 푸시 전용이다")
  void dailyTodo_pushOnly() {
    // given
    NotificationType type = NotificationType.DAILY_TODO;

    // when
    // then
    assertFalse(type.isStorable());
    assertTrue(type.isPushOnly());

  }

  @Test
  @DisplayName("[shouldBeStored / isPushOnly] - TC2 DAILY_TODO를 제외한 나머지는 알림 목록에서 조회가능하애 하기 때문에 저장된다.")
  void others_shouldBeStored() {
    // given
    // when
    // then
    for (NotificationType type : NotificationType.values()) {
      if(type == NotificationType.DAILY_TODO) continue;
      assertTrue(type.isStorable());
      assertFalse(type.isPushOnly());
    }

  }

  @Test
  @DisplayName("[NotificatoinType.DAILY_TODO] - TC3 DAILY_TODO RelatedEntityType은 NONE이다.")
  void DAILY_TODO_is_NONE() {
    // given
    NotificationType type = NotificationType.DAILY_TODO;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.NONE);

  }

  @Test
  @DisplayName("[NotificatoinType.TEUM_REQUEST] - TC4 TEUM_REQUEST의 RelatedEntityType은 TEUM_REQUEST이다.")
  void TEUM_REQUEST_is_TEUM_REQUEST() {
    // given
    NotificationType type = NotificationType.TEUM_REQUEST;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.TEUM_REQUEST);

  }

  @Test
  @DisplayName("[NotificatoinType.TEUM_ACCEPTED] - TC5 TEUM_ACCEPTED RelatedEntityType은 TEUM_RESPONSE이다..")
  void TEUM_ACCEPTED_is_TEUM_RESPONSE() {
    // given
    NotificationType type = NotificationType.TEUM_ACCEPTED;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.TEUM_RESPONSE);

  }

  @Test
  @DisplayName("[NotificatoinType.TEUM_DECLINED] - TC6 TEUM_DECLINED의 RelatedEntityType은 TEUM_RESPONSE이다..")
  void TEUM_DECLINED_is_TEUM_RESPONSE() {
    // given
    NotificationType type = NotificationType.TEUM_DECLINED;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.TEUM_RESPONSE);

  }

  @Test
  @DisplayName("[NotificatoinType.TEUM_SUGGESTED] - TC7 TEUM_SUGGESTED의 RelatedEntityType은 TEUM_RESPONSE이다.")
  void TEUM_SUGGESTED_is_TEUM_RESPONSE() {
    // given
    NotificationType type = NotificationType.TEUM_SUGGESTED;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.TEUM_RESPONSE);

  }

  @Test
  @DisplayName("[NotificatoinType.TEUM_CANCELED] - TC8 TEUM_CANCELED의 RelatedEntityType은 SCHEDULE이다.")
  void TEUM_CANCELED_is_SCHEDULE() {
    // given
    NotificationType type = NotificationType.TEUM_CANCELED;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.SCHEDULE);

  }

  @Test
  @DisplayName("[NotificatoinType.TEUM_REQUEST_REREQUEST] - TC9 TEUM_REQUEST_REREQUEST의 RelatedEntityType은 TEUM_REQUEST이다.")
  void TEUM_REQUEST_REREQUEST_is_TEUM_REQUEST() {
    // given
    NotificationType type = NotificationType.TEUM_REQUEST_REREQUEST;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.TEUM_REQUEST);

  }

  @Test
  @DisplayName("[NotificatoinType.FOLLOW] - TC10 FOLLOW의 RelatedEntityType은 FRIEND이다.")
  void FOLLOW_is_FRIEND() {
    // given
    NotificationType type = NotificationType.FOLLOW;
    RelatedEntityType relatedEntityType = type.getRelatedEntityType();
    // when
    // then
    assertEquals(relatedEntityType, RelatedEntityType.FRIEND);

  }

  @Test
  @DisplayName("[RelationEntityType] - TC11 RelationEntityType enum이 정상적으로 등록되어 있다.")
  void relationEntityType_check() {
    // given
    RelatedEntityType[] values = RelatedEntityType.values();
    // when
    assertEquals(6, values.length);
    // then
    assertNotNull(RelatedEntityType.valueOf("NONE"));
    assertNotNull(RelatedEntityType.valueOf("TEUM_REQUEST"));
    assertNotNull(RelatedEntityType.valueOf("TEUM_RESPONSE"));
    assertNotNull(RelatedEntityType.valueOf("SCHEDULE"));
    assertNotNull(RelatedEntityType.valueOf("FRIEND"));
    assertNotNull(RelatedEntityType.valueOf("REMINDER"));
  }


}
