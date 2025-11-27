package umc.teumteum.server.domain.friend.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.friend.entity.Block;
import umc.teumteum.server.domain.user.entity.User;

@Component
public class BlockConverter {

    public static Block toBlock(User blocker, User blocked) {
        return Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
    }
}