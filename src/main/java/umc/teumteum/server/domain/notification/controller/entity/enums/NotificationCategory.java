package umc.teumteum.server.domain.notification.controller.entity.enums;

public enum NotificationCategory {
    TODAY_TODO,      // 오늘의 일정
    REMIND_ALARM,    // 리마인드 알림
    RECEIVE_REQUEST, // 틈요청 도착
    ACCEPT_REQUEST,  // 틈요청 수락
    REJECT_REQUEST,  // 틈요청 거절
    CHANGE_TIME,     // 틈 시간 변경
    CANCELED         // 틈 취소
}
