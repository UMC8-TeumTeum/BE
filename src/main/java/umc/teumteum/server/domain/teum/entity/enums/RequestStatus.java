package umc.teumteum.server.domain.teum.entity.enums;

public enum RequestStatus {
    ACTIVE,    // 진행 중
    CLOSED,    // 자동 마감 (시간 지남)
    CANCELED,  // 취소
}
