package umc.teumteum.server.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.home.converter.ScheduleConverter;
import umc.teumteum.server.domain.home.dto.CreateTodoRequestDTO;
import umc.teumteum.server.domain.home.dto.CreateTodoResponseDTO;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final ScheduleConverter scheduleConverter;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;

    // Todo 등록
    @Override
    public CreateTodoResponseDTO createTodo(CreateTodoRequestDTO dto) {

        // 스케줄 저장
        Schedule schedule = scheduleConverter.toSchedule(dto);
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 스케줄 리마인드 알림 저장
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders = scheduleConverter.toScheduleReminders(savedSchedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }

        return new CreateTodoResponseDTO(savedSchedule.getId());
    }
}
