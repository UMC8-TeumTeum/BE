package umc.teumteum.server.domain.user.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4040", "존재하지 않는 사용자입니다."),


    // 사용자 온보딩
    INVALID_STEP(HttpStatus.BAD_REQUEST, "ONBOARDING4001", "현재 진행할 수 있는 단계가 아닙니다."),
    TOS_CONSENT_NOT_AGREED(HttpStatus.BAD_REQUEST, "ONBOARDING4002", "서비스 이용약관은 필수 동의 항목입니다."),
    PRIVACY_CONSENT_NOT_AGREED(HttpStatus.BAD_REQUEST, "ONBOARDING4003", "개인정보 수집 및 이용은 필수 동의 항목입니다."),
    UNSUPPORTED_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "ONBOARDING4004", "지원하지 않는 이미지 형식입니다."),
    INVALID_IMAGE_NAME(HttpStatus.BAD_REQUEST, "ONBOARDING4005", "잘못된 파일명에 대한 등록 요청입니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "ONBOARDING4006", "시작/종료 시간이 잘못 설정된 반복 일정이 존재합니다."),
    ROUTINE_TIME_CONFLICT(HttpStatus.BAD_REQUEST, "ONBOARDING4007", "반복 일정 간 시간 충돌이 발생했습니다."),
    ROUTINE_SLEEP_CONFLICT(HttpStatus.BAD_REQUEST, "ONBOARDING4008", "수면 패턴과 반복 일정 간 시간 충돌이 발생했습니다."),
    INVALID_REMIND_ALARM_VALUE(HttpStatus.BAD_REQUEST, "ONBOARDING4009", "리마인드 알림은 1분, 3분, 5분, 10분, 30분 중에서만 설정 가능합니다."),
    EXPIRED_UPLOAD_SESSION(HttpStatus.BAD_REQUEST, "ONBOARDING4010", "프로필 이미지 업로드 세션이 만료되었습니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "ONBOARDING4091", "이미 사용 중인 닉네임입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
