package umc.teumteum.server.domain.user.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

@Component
@RequiredArgsConstructor
public class UserConverter {

    private final S3Util s3Util;

    public UserSearchResponseDto toSearchResponseDto(User user) {
        return new UserSearchResponseDto(
                user.getId(),
                user.getNickname(),
                s3Util.toUrl(user.getProfileImageKey()),
                user.getJob()
        );
    }
}
