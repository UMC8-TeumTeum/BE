package umc.teumteum.server.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.converter.ScheduleConverter;
import umc.teumteum.server.domain.home.dto.TodoRequestDTO;
import umc.teumteum.server.domain.home.dto.TodoIdResponseDTO;
import umc.teumteum.server.domain.home.dto.TodoInfoResponseDTO;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.exception.HomeErrorStatus;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.util.S3Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final ScheduleConverter scheduleConverter;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final UserRepository userRepository;
    private final S3Util s3Util;

    @Transactional
    @Override
    public TodoIdResponseDTO createTodo(TodoRequestDTO dto) {
        // Todo 등록
        // 종료 시간이 시작 시간보다 빠르면 예외 발생
        if (dto.getEndTime().isBefore(dto.getStartTime())){
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 스케줄 저장
        Schedule schedule = scheduleConverter.toSchedule(dto);
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 스케줄 리마인드 알림 저장
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders = scheduleConverter.toScheduleReminders(savedSchedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new TodoIdResponseDTO(savedSchedule.getId());
    }

    @Override
    public TodoInfoResponseDTO getTodoInfo(Long scheduleId) {
        // Todo(Schedule) 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        // 라미인드 알림 조회
        List<ScheduleReminder> reminders = scheduleReminderRepository.findByScheduleId(schedule.getId());

        // 프로필 조회
        List<String> profileUrls;
        if(schedule.getType() == ScheduleType.TEUM){ // TEUM 타입인 경우
            profileUrls = getTeumProfileUrls(schedule);
        } else{
            String profileImageKey = schedule.getUser().getProfileImageKey();
            profileUrls = profileImageKey != null ? List.of(s3Util.toUrl(profileImageKey)) : List.of();
        }

        return scheduleConverter.toTodoInfoResponse(schedule,reminders, profileUrls);
    }

    private List<String> getTeumProfileUrls(Schedule schedule){
        // TEUM 타입 프로필 조회
        Long teumRequestId = schedule.getTeumRequest().getId();

        // 1. 틈 요청한 사람 ID 조회
        TeumRequest teumRequest = teumRequestRepository.findById(teumRequestId)
                .orElseThrow(()-> new GeneralException(TeumErrorStatus.TEUM_REQUEST_NOT_FOUND));

        Long requestUserId =  teumRequest.getUser().getId();

        // 2. 수락한 응답자 ID
        List<Long> acceptedUserIds = teumRequest.getTeumResponses().stream()
                .filter(r -> r.getStatus() == ResponseStatus.ACCEPTED)
                .map(r -> r.getReceiverUser().getId())
                .toList();

        // 두 개 합쳐서 반환
        List<Long> userIds = new ArrayList<>(acceptedUserIds);
        userIds.add(requestUserId);

        return userRepository.findAllById(userIds).stream()
                .map(User::getProfileImageKey)
                .filter(Objects::nonNull)
                .map(s3Util::toUrl)
                .toList();
    }

    @Transactional
    @Override
    public TodoIdResponseDTO updateTodoInfo(TodoRequestDTO dto, Long scheduleId) {
        // Todo(Schedule) 수정
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        // 종료 시간이 시작 시간보다 빠르면 예외 발생
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 스케줄 필드 업데이트
        schedule.updateField(dto);

        // 스케줄 리마인드 알림 저장
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            scheduleReminderRepository.deleteByScheduleId(scheduleId);
            List<ScheduleReminder> reminders = scheduleConverter.toScheduleReminders(schedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new TodoIdResponseDTO(schedule.getId());
    }

    @Transactional
    @Override
    public void deleteTodo(Long scheduleId) {
        // Todo(Schedule) 삭제
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        scheduleRepository.deleteById(scheduleId);
        scheduleReminderRepository.deleteByScheduleId(scheduleId);
    }
}
