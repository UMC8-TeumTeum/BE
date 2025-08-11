package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.AuthRequestDto;
import umc.teumteum.server.domain.auth.dto.AuthResponseDto;

public interface AuthService {
    AuthResponseDto.LoginResponse socialLogin(String socialType, AuthRequestDto.SocialLoginRequest request);

    AuthResponseDto.DevTokenResponse generateDevAccessToken();

    AuthResponseDto.ReissueResponse reissueToken(AuthRequestDto.ReissueRequest request);
}
