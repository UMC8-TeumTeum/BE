package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.user.converter.AgreementConverter;
import umc.teumteum.server.domain.user.converter.UserConverter;
import umc.teumteum.server.domain.user.dto.UserRequestDTO;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.exception.UserHandler;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.AgreementRepository;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.util.S3Util;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;

    // 온보딩 - 약관 동의
    @Override
    @Transactional
    public void saveAgreement(UserRequestDTO.AgreeRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.AGREEMENT);

        // 2. 기존 동의 이력이 있는지 확인
        if (agreementRepository.existsByUser(user)) {
            throw new UserHandler(UserErrorStatus.AGREEMENT_ALREADY_EXISTS);
        }

        // 3. 필수 항목 동의 여부 확인
        if (!request.getTosConsent()) {
            throw new UserHandler(UserErrorStatus.TOS_CONSENT_NOT_AGREED);
        }
        if (!request.getPrivacyConsent()) {
            throw new UserHandler(UserErrorStatus.PRIVACY_CONSENT_NOT_AGREED);
        }

        // 4. Entity 변환
        Agreement agreement = AgreementConverter.toAgreement(request, user);

        // 5. 저장
        agreementRepository.save(agreement);

        // 6. 사용자 step 변경
        user.updateStep(UserStep.ONBOARDING);
    }


    // 온보딩 - 닉네임 & 분야/직종 등록
    @Override
    @Transactional
    public void saveNicknameAndJob(UserRequestDTO.NicknameJobRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. 닉네임 중복 여부 확인
        // 기존 닉네임이 null이면(=처음 닉네임 등록) 단순 중복 체크
        if (user.getNickname() == null) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new UserHandler(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
            }
        }
        // 기존 닉네임이 있으면(=온보딩 중단으로 인한 닉네임 재등록) 본인 닉네임 이외와 중복 체크
        else {
            if (!request.getNickname().equals(user.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
                throw new UserHandler(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
            }
        }

        // 3. 닉네임과 분야/직종 수정
        user.updateNicknameAndJob(request.getNickname(), request.getJobField());
    }


    @Override
    @Transactional
    public void saveSleepPattern(UserRequestDTO.SleepPatternRequest request, User user) {

    }


    // 사용자의 step을 확인
    private void validateOnboardingStep(User user, UserStep expectedStep) {
        if (user.getStep() == null || !user.getStep().equals(expectedStep)) {
            throw new UserHandler(UserErrorStatus.INVALID_STEP);
        }
    }
}
