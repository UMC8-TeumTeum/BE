package umc.teumteum.server.domain.user.converter;

import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class RoutineConverter {

    public static Routine toRoutine(OnboardingRequestDto.RoutineDTO request, User user) {
        return Routine.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .weekday(request.getWeekday())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build()
                ;
    }

    public static List<Routine> toRoutineList(List<OnboardingRequestDto.RoutineDTO> requests, User user) {
        return requests.stream()
                .map(request -> toRoutine(request, user))
                .collect(Collectors.toList())
                ;
    }

}
