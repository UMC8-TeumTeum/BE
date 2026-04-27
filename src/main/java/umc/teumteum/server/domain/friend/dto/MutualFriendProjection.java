package umc.teumteum.server.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MutualFriendProjection {

    private Long userId;
    private String nickname;
    private String profileImageName;
}