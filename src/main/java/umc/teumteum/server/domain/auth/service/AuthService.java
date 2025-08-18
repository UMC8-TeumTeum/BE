package umc.teumteum.server.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import umc.teumteum.server.domain.auth.dto.AuthRequestDto;
import umc.teumteum.server.domain.auth.dto.AuthResponseDto;
import umc.teumteum.server.domain.user.entity.User;

public interface AuthService {
    AuthResponseDto.LoginResponse socialLogin(String socialType, AuthRequestDto.SocialLoginRequest request);

    AuthResponseDto.DevTokenResponse generateDevAccessToken();

    AuthResponseDto.ReissueResponse reissueToken(AuthRequestDto.ReissueRequest request);

    void logout(HttpServletRequest httpServletRequest, User user);
}
