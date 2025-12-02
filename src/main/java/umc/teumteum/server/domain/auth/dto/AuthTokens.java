package umc.teumteum.server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokens {

    private String accessToken;     // AT
    private String refreshToken;    // RT
}
