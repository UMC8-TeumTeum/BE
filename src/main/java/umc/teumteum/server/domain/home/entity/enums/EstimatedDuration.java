package umc.teumteum.server.domain.home.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstimatedDuration {
    MINUTES_10("10m"),    // 10분
    MINUTES_20("20m"),    // 20분
    MINUTES_30("30m"),    // 30분
    HOUR_1("1h");         // 1시간 이상

    private final String displayName;

    EstimatedDuration(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static EstimatedDuration from(String value) {
        for (EstimatedDuration estimatedDuration : values()) {
            if (estimatedDuration.displayName.equals(value)) {
                return estimatedDuration;
            }
        }
        return null;
    }

}
