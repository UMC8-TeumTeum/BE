package umc.teumteum.server.domain.notification.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.home.entity.enums.DispatchStatus;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.notification.converter.NotificationConverter;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.notification.entity.enums.RelatedEntityType;
import umc.teumteum.server.domain.notification.exception.NotificationException;
import umc.teumteum.server.domain.notification.exception.status.NotificationErrorStatus;
import umc.teumteum.server.domain.notification.repository.NotificationRepository;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final TeumResponseRepository teumResponseRepository;
  private final TeumRequestRepository teumRequestRepository;
  private final FriendRepository friendRepository;
  private final ScheduleReminderRepository scheduleReminderRepository;

  private final S3Util s3Util;

  @Override
  public NotificationResponseDto.SliceResponseDto getNotifications(User user, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));


    Slice<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable); //User에 상대방 정보 담겨 있으니까.. 일단 상대방 정보를 기준으로 알림 조회
    Map<Long, User> relatedUserMap = resolveRelatedUsers(notifications.getContent());
    Map<Long, LocalDateTime> eventDateMap = resolveEventDates(notifications.getContent());

    List<NotificationResponseDto.NotificationDto> notificationList = notifications.stream()
        .filter(n->isValidNotification(n, relatedUserMap))
        .map(n-> {
          User friend = relatedUserMap.get(n.getId());
          LocalDateTime eventDate = eventDateMap.get(n.getId());

          String profileImageUrl = s3Util.toPresignedUrl("profile/" + friend.getProfileImageName(), Duration.ofMinutes(30));
          return NotificationConverter.toNotificationDto(n, friend, profileImageUrl,eventDate);
        })
        .toList();

    return NotificationConverter.toSliceResponseDto(notificationList, notifications.hasNext(), page, size);

  }

  private Map<Long, User> resolveRelatedUsers(List<Notification> notifications) {
    Map<Long, User> result = new HashMap<>();

    for (Notification notification : notifications) {
      RelatedEntityType type = notification.getType().getRelatedEntityType();
      Long relatedId = notification.getRelatedId();

      switch (type) {
        case TEUM_REQUEST -> {
          teumRequestRepository.findById(relatedId)
                  .ifPresent(r -> result.put(notification.getId(), r.getUser()));
        }
        case TEUM_RESPONSE -> {
          teumResponseRepository.findById(relatedId)
                  .ifPresent(r -> result.put(notification.getId(), r.getReceiverUser()));
        }
        case FRIEND -> {
          friendRepository.findById(relatedId)
                  .ifPresent(f -> result.put(notification.getId(), f.getFollower()));
        }
        case SCHEDULE -> {
          teumResponseRepository.findById(relatedId)
                  .ifPresent(r -> result.put(notification.getId(), r.getReceiverUser()));
        }
        default -> {
          log.warn("상대 유저를 해석할 수 없는 알림. id={}, type={}",
                  notification.getId(), type);
        }
      }
    }
    return result;
  }

  private Map<Long, LocalDateTime> resolveEventDates(List<Notification> notifications) {
    Map<Long, LocalDateTime> result = new HashMap<>();

    for (Notification notification : notifications) {
      RelatedEntityType type = notification.getType().getRelatedEntityType();
      Long relatedId = notification.getRelatedId();

      switch (type) {
        case TEUM_REQUEST -> {
          teumRequestRepository.findById(relatedId)
                  .ifPresent(r ->
                          result.put(
                                  notification.getId(),
                                  LocalDateTime.of(r.getDate(), r.getStartTime())
                          )
                  );
        }
        case TEUM_RESPONSE -> {
          teumResponseRepository.findById(relatedId)
                  .map(TeumResponse::getTeumRequest)
                  .ifPresent(req ->
                          result.put(
                                  notification.getId(),
                                  LocalDateTime.of(req.getDate(), req.getStartTime())
                          )
                  );
        }

        // SCHEDULE/FRIEND는 date 없음
        default -> {}
      }
    }
    return result;
  }



  private boolean isValidNotification(Notification notificaion, Map<Long, User> relatedUserMap) {
    boolean exists = relatedUserMap.containsKey(notificaion.getId());
    if (!exists) {
      log.warn("연결된 유저가 없어 필터링된 알림입니다. 알림 id={}", notificaion.getId());
    }
    return exists;
  }

  // DispatchStatus 업데이트
  @Transactional
  @Override
  public void updateDispatchStatus(List<Long> sentIds, List<Long> failIds) {

    if (sentIds != null && !sentIds.isEmpty()) {
      scheduleReminderRepository.updateDispatchStatusByIds(
              sentIds,
              DispatchStatus.PROCESSING,
              DispatchStatus.SENT
      );
    }
    if (failIds != null && !failIds.isEmpty()) {
      scheduleReminderRepository.updateDispatchStatusByIds(
              failIds,
              DispatchStatus.PROCESSING,
              DispatchStatus.FAILED
      );
    }

  }

  @Transactional
  @Override
  public NotificationResponseDto.ReadResponseDto readNotification(User user, Long notificationId) {

    Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationException(NotificationErrorStatus.NOTIFICATION_NOT_FOUND));

    if (!notification.getUser().getId().equals(user.getId())) {
      throw new NotificationException(NotificationErrorStatus.FORBIDDEN_NOTIFICATION);
    }

    if (!Boolean.TRUE.equals(notification.getIsRead())) {
      notification.markAsRead();
    }

    return NotificationConverter.toReadResponseDto(notification);
  }


}
