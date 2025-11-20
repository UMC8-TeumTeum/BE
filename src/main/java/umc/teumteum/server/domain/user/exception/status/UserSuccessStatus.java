package umc.teumteum.server.domain.user.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum UserSuccessStatus implements BaseCode {

    _USER_FOUND(HttpStatus.OK, "USER2001", "사용자 조회 성공"),
    _USER_PRESIGNED_URL_ISSUED(HttpStatus.OK,"USER2002","프로필 이미지 업로드용 Presigned URL 발급이 완료되었습니다."),
    _PROFILE_IMAGE_UPDATED(HttpStatus.OK,"USER2003","프로필 이미지 수정이 완료되었습니다."),
    _PROFILE_IMAGE_DELETED(HttpStatus.OK,"USER2004","프로필 이미지 삭제가 완료되었습니다."),
    _PROFILE_UPDATED(HttpStatus.OK,"USER2005","개인정보 수정이 완료되었습니다."),
    _ALARM_STATE_UPDATED(HttpStatus.OK,"USER2006","사용자의 알림 설정 변경이 완료되었습니다."),
    _ROUTINE_LOADED(HttpStatus.OK,"USER2007","반복일정 조회가 완료되었습니다."),
    _ROUTINE_DELETED(HttpStatus.OK,"USER2008","반복일정 삭제가 완료되었습니다."),
    _ROUTINE_ADDED(HttpStatus.OK,"USER2009","반복일정 등록이 완료되었습니다."),



    // 사용자 온보딩
    AGREEMENT_SAVED(HttpStatus.OK, "ONBOARDING2001", "약관 동의가 완료되었습니다."),
    NICKNAME_JOB_SAVED(HttpStatus.OK, "ONBOARDING2002", "닉네임과 분야/직종 등록이 완료되었습니다."),
    PRESIGNED_URL_ISSUED(HttpStatus.OK, "ONBOARDING2003", "프로필 이미지 업로드용 Presigned URL 발급이 완료되었습니다."),
    PROFILE_IMAGE_SAVED(HttpStatus.OK, "ONBOARDING2004", "프로필 이미지 등록이 완료되었습니다."),
    SLEEP_PATTERN_SAVED(HttpStatus.OK, "ONBOARDING2005", "수면 패턴 등록이 완료되었습니다."),
    ROUTINE_SAVED(HttpStatus.OK, "ONBOARDING2006", "요일별 반복 일정 등록이 완료되었습니다."),
    REMIND_ALARM_SAVED(HttpStatus.OK, "ONBOARDING2007", "리마인드 알림 설정 등록이 완료되었습니다.");


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .isSuccess(true)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(true)
                .code(code)
                .message(message)
                .build();
    }
}
