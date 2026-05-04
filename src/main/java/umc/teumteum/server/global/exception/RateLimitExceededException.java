package umc.teumteum.server.global.exception;

import lombok.Getter;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.ratelimit.RateLimitPolicy;

@Getter
public class RateLimitExceededException extends GeneralException {

    private final RateLimitPolicy policy;

    public RateLimitExceededException(RateLimitPolicy policy) {
        super(ErrorStatus.RATE_LIMIT_EXCEEDED);
        this.policy = policy;
    }
}
