package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
import umc.teumteum.server.domain.user.entity.enums.SocialType;

public interface AuthService {
    AuthResponseDTO.LoginResponse socialLogin(AuthRequestDTO.SocialLoginRequest request);
}
