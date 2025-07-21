package umc.teumteum.server.domain.notification.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.notification.converter.NotificationConverter;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.notification.entity.enums.RelatedEntityType;
import umc.teumteum.server.domain.notification.repository.NotificationRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.GlobalHandler;
import umc.teumteum.server.global.util.S3Util;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final TeumResponseRepository teumResponseRepository;
  private final TeumRequestRepository teumRequestRepository;
  private final FriendRepository friendRepository;

  private final S3Util s3Util;

  @Override
  public List<NotificationResponseDto> getNotifications(User user) {

    List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user); //User에 상대방 정보 담겨 있으니까.. 일단 상대방 정보를 기준으로 알림 조회
    Map<Long, User> relatedUserMap = resolveRelatedUsers(notifications);
    return NotificationConverter.toNotificationResponse(notifications, relatedUserMap, s3Util);

  }

  private User getUserOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalHandler(ErrorStatus.INVALID_USER));
  }

  private Map<Long, User> resolveRelatedUsers(List<Notification> notifications) {
    Map<Long, User> result = new HashMap<>();

    Map<RelatedEntityType, List<Notification>> grouped = notifications.stream()
        .collect(Collectors.groupingBy(n -> n.getType().getRelatedEntityType()));

    // 틈 응답들 (틈 수락,틈 거절,틈 제안)
    if (grouped.containsKey(RelatedEntityType.TEUM_RESPONSE)) {
      List<Long> ids = grouped.get(RelatedEntityType.TEUM_RESPONSE).stream()
          .map(Notification::getRelatedId).toList();
      List<TeumResponse> responses = teumResponseRepository.findAllById(ids);
      responses.forEach(r -> result.put(r.getId(), r.getReceiverUser()));
    }

    // 틈 요청
    if (grouped.containsKey(RelatedEntityType.TEUM_REQUEST)) {
      List<Long> ids = grouped.get(RelatedEntityType.TEUM_REQUEST).stream()
          .map(Notification::getRelatedId).toList();
      List<TeumRequest> requests = teumRequestRepository.findAllById(ids);
      requests.forEach(r -> result.put(r.getId(), r.getUser()));
    }

    // 요청 수락했다가 취소된 경우
    if(grouped.containsKey(RelatedEntityType.SCHEDULE)) {
      List<Long> ids = grouped.get(RelatedEntityType.SCHEDULE).stream()
          .map(Notification::getRelatedId).toList();
      List<TeumResponse> responses = teumResponseRepository.findAllById(ids);
      responses.forEach(r -> result.put(r.getId(), r.getReceiverUser()));
    }

    if (grouped.containsKey(RelatedEntityType.FRIEND)) {
      List<Long> ids = grouped.get(RelatedEntityType.FRIEND).stream() //친구 ID를 저장해야됨. 그리고 팔로워Id를 조회해야됨
          .map(Notification::getRelatedId).toList();                  //친구 ID를 조회
      List<Friend> friends = friendRepository.findAllById(ids);
      friends.forEach(f -> result.put(f.getId(), f.getFollower()));
    }

    return result;
  }
}
