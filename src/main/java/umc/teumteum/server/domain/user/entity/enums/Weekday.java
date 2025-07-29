package umc.teumteum.server.domain.user.entity.enums;

import java.time.DayOfWeek;

public enum Weekday {
    MONDAY,    // 월요일
    TUESDAY,   // 화요일
    WEDNESDAY, // 수요일
    THURSDAY,  // 목요일
    FRIDAY,    // 금요일
    SATURDAY,  // 토요일
    SUNDAY     // 일요일
    ;

    public boolean matches(DayOfWeek dayOfWeek) {
        return this.name().equalsIgnoreCase(dayOfWeek.name());
    }

    public static Weekday from(DayOfWeek dayOfWeek) {
        return Weekday.valueOf(dayOfWeek.name());
    }
}
