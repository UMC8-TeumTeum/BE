package umc.teumteum.server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokens {

    private final String accessToken;     // AT
    private final String refreshToken;    // RT
}
