package umc.teumteum.server.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO.LoginResponse socialLogin(String socialType, AuthRequestDTO.SocialLoginRequest request);

    AuthResponseDTO.DevTokenResponse generateDevAccessToken();

    AuthResponseDTO.ReissueResponse reissueToken(AuthRequestDTO.ReissueRequest request);

    void logout(HttpServletRequest httpServletRequest);
}
