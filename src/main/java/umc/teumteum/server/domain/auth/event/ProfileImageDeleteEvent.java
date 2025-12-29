package umc.teumteum.server.domain.auth.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileImageDeleteEvent {

    private final Long userId;
    private final String imageName;
}
