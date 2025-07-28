package umc.teumteum.server.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.global.dto.TimeRange;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TimeUtil {

    // 공통 - 정렬된 일정들의 시간 충돌 확인
    public void validateTimeRangeConflicts(List<TimeRange> timeRanges, UserErrorStatus errorStatus) {
        // 1. 시작 시간 기준으로 정렬
        timeRanges.sort(Comparator.comparing(TimeRange::getStartTime));

        // 2. 인접한 시간 범위들 순차적으로 충돌 확인
        for (int i = 0; i < timeRanges.size() - 1; i++) {
            LocalTime currentEnd = convertEndTime(timeRanges.get(i).getEndTime());
            LocalTime nextStart = timeRanges.get(i + 1).getStartTime();

            // 앞일정의 종료시간 > 뒤일정의 시작시간  (=시간 충돌)
            if (currentEnd.isAfter(nextStart)) {
                throw new GlobalHandler(errorStatus);
            }
        }
    }


    // 공통 - 종료시간 변환 (종료시간이 00:00인 경우 LocalTime.MAX로 변환 필요)
    public LocalTime convertEndTime(LocalTime endTime) {
        return endTime.equals(LocalTime.MIDNIGHT) ? LocalTime.MAX : endTime;
    }
}
