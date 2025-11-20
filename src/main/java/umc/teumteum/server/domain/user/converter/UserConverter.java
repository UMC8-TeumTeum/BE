package umc.teumteum.server.domain.user.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class UserConverter {

    private final S3Util s3Util;

    public UserSearchResponseDto toSearchResponseDto(User user) {
        return new UserSearchResponseDto(
                user.getId(),
                user.getNickname(),
                s3Util.toPresignedUrl("profile/" + user.getProfileImageName(), Duration.ofMinutes(30)),
                user.getJob()
        );
    }

    public static UserResponseDTO.MyPageDTO toMyPageDTO(User user, String profileImageUrl) {
        return UserResponseDTO.MyPageDTO.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImageUrl(profileImageUrl)
            .job(user.getJob())
            .build();

    }

    // routine -> UserResponseDTO.RoutineDTO
    public static UserResponseDTO.RoutineDTO toRoutineDTO(Routine routine) {
        return UserResponseDTO.RoutineDTO.builder()
                .title(routine.getTitle())
                .description(routine.getDescription())
                .weekday(routine.getWeekday())
                .startTime(routine.getStartTime())
                .endTime(routine.getEndTime())
                .build();
    }
}
