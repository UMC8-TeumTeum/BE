package umc.teumteum.server.domain.notification.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.DispatchStatus;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class NotificationUseCases {
  // 팔로우(1:1), 틈요청(1:1, 1:다), 틈응답(1:1), 리마인드 알림
  private final NotificationOrchestrator orchestrator;
  private final ScheduleReminderRepository scheduleReminderRepository;

  // 팔로우 알림
  public void notifyFollow(User sender, User reciver, Long friendId){
    String content = NotificationType.FOLLOW.getContent();
    orchestrator.saveAndPush(
        reciver,
        NotificationType.FOLLOW,
        content,
        friendId,
        Map.of(
            "senderId", String.valueOf(sender.getId()),
            "senderName", sender.getNickname())
    );
  }

  // 틈 요청 알림(1:1) : sender -> receiver
  public void notifyTeumRequest(User sender, User reciver, Long requestId, TeumRequestDto.TeumRequest request) {
    String content = NotificationType.TEUM_REQUEST.getContent();

    Map<String, String> data = new java.util.HashMap<>();
    data.put("senderId", String.valueOf(sender.getId()));
    data.put("senderName", sender.getNickname());
    data.put("title", request.getTitle());
    data.put("description", request.getDescription() == null ? "" : request.getDescription());
    data.put("date", request.getDate());
    data.put("startTime", request.getStartTime());
    data.put("endTime", request.getEndTime());
    data.put("graphicId", String.valueOf(request.getGraphicId()));
    data.put("isGroup", "false");
    data.put("othersCount", "0");



    orchestrator.saveAndPush(
        reciver,
        NotificationType.TEUM_REQUEST,
        content,
        requestId,
        data
    );
  }

  // 틈 응답 알림
  public void notifyTeumResponse(User sender, User receiver, Long teumResponseId, boolean accepted) {
    NotificationType type = accepted ? NotificationType.TEUM_ACCEPTED : NotificationType.TEUM_DECLINED;
    String content = type.getContent();

    Map<String, String> data = new HashMap<>();
    data.put ("senderId", String.valueOf(sender.getId())) ;
    data.put ("senderName", sender.getNickname());
    data.put ("accepted", String.valueOf(accepted) ) ;


    orchestrator.saveAndPush(
        receiver,
        type,
        content,
        teumResponseId,
        data
    );

  }
  // 일대일 재요청
  public void notifyTeumReRequest(User sender, User receiver, Long requestId, TeumRequest parentRequest) {
    String content = NotificationType.TEUM_REQUEST_REREQUEST.getContent();

    Map<String, String> data = new HashMap<>();
    data.put("senderId", String.valueOf(sender.getId()));
    data.put("senderName", sender.getNickname());
    data.put("title", parentRequest.getTitle());
    data.put("description", parentRequest.getDescription() == null ? "" : parentRequest.getDescription());
    data.put("date", String.valueOf(parentRequest.getDate()));
    data.put("startTime", String.valueOf(parentRequest.getStartTime()));
    data.put("endTime", String.valueOf(parentRequest.getEndTime()));
    data.put("graphicId", String.valueOf(parentRequest.getGraphicId()));
    data.put("isGroup", "false");
    data.put("othersCount", "0");
    data.put("reRequest", "true");
    data.put("originalRequestId", String.valueOf(parentRequest.getId()));


    orchestrator.saveAndPush(
        receiver,
        NotificationType.TEUM_REQUEST_REREQUEST,
        content,
        requestId,
        data
    );
  }

  // 리마인드 알림
  @Transactional
  public void notifyReminder(List<ScheduleReminder> reminders){
    if (reminders == null || reminders.isEmpty()) return;

    List<Long> sentIds = new ArrayList<>();

    for(ScheduleReminder r : reminders){
      Schedule schedule = r.getSchedule();
      User receiver = schedule.getUser();

      int minutes = r.getReminderTime();
      String content = minutes + "분 뒤 투두가 시작돼요";

      Map<String,String> data = new HashMap<>();
      data.put("scheduleId", String.valueOf(schedule.getId()));
      data.put("title", schedule.getTitle());
      data.put("startTime", String.valueOf(schedule.getStartTime()));
      data.put("endTime", String.valueOf(schedule.getEndTime()));
      data.put("reminderMinutes", String.valueOf(r.getReminderTime()));

      orchestrator.saveAndPush(
              receiver,
              NotificationType.REMIND_ALARM,
              content,
              schedule.getId(),
              data
      );

      sentIds.add(r.getId());
    }

    scheduleReminderRepository.updateDispatchStatusByIds(
            sentIds,
            DispatchStatus.PROCESSING,
            DispatchStatus.SENT
    );
  }
}
