package umc.teumteum.server.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.dto.UserRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.Weekday;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserSearchResponseDto> searchUsersByKeyword(String keyword, Long userId);

    User findOrCreateUser(OAuthUserInfo userInfo);

    User createDevUser();

    Optional<User> findUser(Long userId);

    UserResponseDTO.MyPageDTO getMyPage(User user);

    void updateProfile(UserRequestDto.ProfileRequest request, User user);

    OnboardingResponseDto.ProfileImagePresignedUrlResponse generateProfileImagePresignedUrl(HttpServletRequest httpServletRequest, OnboardingRequestDto.ProfileImagePresignedUrlRequest request, User user);

    void saveProfileImage(HttpServletRequest httpServletRequest, OnboardingRequestDto.ProfileImageRequest request, User user);

    void deleteProfileImage(User user);

    void updateAlarm(UserRequestDto.NotificationSettingRequest request, User user);

    List<UserResponseDTO.RoutineDTO> getRoutines(Weekday weekday, User user);

    void deleteRoutine(Long routineId);

    void saveRoutine(OnboardingRequestDto.RoutineDTO request, User user);
}
