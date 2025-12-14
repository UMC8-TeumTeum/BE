package umc.teumteum.server.domain.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import umc.teumteum.server.global.util.S3Util;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileImageListener {

    private final S3Util s3Util;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void delete(ProfileImageDeleteEvent event) {
        try {
            s3Util.deleteObject("profile/" + event.getImageName());
        } catch (Exception e) {
            log.error("S3 프로필 이미지 삭제 실패 - userId: {}, imageName: {}, error: {}",
                    event.getUserId(), event.getImageName(), e.getMessage());
        }
    }
}
