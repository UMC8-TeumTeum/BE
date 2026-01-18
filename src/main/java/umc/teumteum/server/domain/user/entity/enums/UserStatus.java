package umc.teumteum.server.domain.user.entity.enums;

public enum UserStatus {
    ACTIVE,    // 활성화
    INACTIVE,  // 일반 회원 탈퇴
    SUSPENDED, // 임시 정지 (1개월 - 데이터 유지)
    BANNED     // 영구 정지 (3회 적발 - 익명화 처리)
}