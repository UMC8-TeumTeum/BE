package umc.teumteum.server.domain.home.entity.enums;

public enum DispatchStatus {
    PENDING,    // 발송예정
    PROCESSING, // 발송중 (중복방지용)
    SENT,       // 발송완료
    CANCELLED,  // 스케줄 취소, 설정 변경으로 발송 취소
    FAILED      // 발송 실패
}
