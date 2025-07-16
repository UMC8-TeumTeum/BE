package umc.teumteum.server.domain.teum.entity.enums;

public enum ResponseStatus {
    PENDING,    // 수신자가 응답을 하지 않음
    ACCEPTED,   // 수신자가 요청을 수락
    REJECTED,   // 수신자가 요청을 거절
    RESEND   // 수신자가 시간대 바꿔 재요청
}
