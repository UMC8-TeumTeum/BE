package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO.LoginResponse socialLogin(String socialType, AuthRequestDTO.SocialLoginRequest request);

    AuthResponseDTO.DevTokenResponse generateDevAccessToken();
}
