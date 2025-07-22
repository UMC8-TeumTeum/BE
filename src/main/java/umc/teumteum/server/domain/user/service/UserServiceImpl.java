package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.converter.AgreementConverter;
import umc.teumteum.server.domain.user.converter.UserConverter;
import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.dto.UserRequestDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.exception.UserHandler;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.AgreementRepository;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;
    private final RemindAlarmRepository remindAlarmRepository;

    @Override
    public List<UserSearchResponseDto> searchUsersByKeyword(String keyword, Long requesterId) {
        String keywordLower = keyword.toLowerCase();
        LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();

        Comparator<Map.Entry<User, Integer>> byDistance = Comparator.comparingInt(Map.Entry::getValue);

        return userRepository.findByNicknameContaining(keyword).stream()
                .filter(user -> !user.getId().equals(requesterId))
                .map(user -> Map.entry(user,
                        distanceCalculator.apply(keywordLower, user.getNickname().toLowerCase())))
                .sorted(byDistance)
                .limit(5)
                .map(entry -> userConverter.toSearchResponseDto(entry.getKey()))
                .toList();
    }


    @Override
    public List<PublicTodoResponseDto> getRecentPublicTodos(Long userId) {
        // TODO : 최근 공개 투두 2개 조회 로직 구현
        return List.of();
    }

    @Override
    public List<PublicTodoResponseDto> getDailyPublicTodos(Long userId, String date) {
        // TODO : 특정 날짜의 공개 투두 조회 로직 구현
        return List.of();
    }

    @Override
    public List<String> getTodoDatesOfMonth(Long userId, String month) {
        // TODO : 공개 투두가 있는 날짜 조회 로직 구현
        return List.of();
    }


    // 소셜 로그인 시, 사용자 조회 (없으면 생성)
    @Override
    @Transactional
    public User findOrCreateUser(OAuthUserInfo userInfo) {

        SocialType socialType = userInfo.getSocialType();
        String socialId = userInfo.getSocialId();
        String email = userInfo.getEmail();

        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .socialType(socialType)
                            .socialId(socialId)
                            .email(email)
                            .build()
                            ;

                    return userRepository.save(newUser);
                });
    }


    // 개발용 액세스 토큰 사용자 생성
    @Override
    @Transactional
    public User createDevUser() {
        return userRepository.findByEmail("teumteum@kakao.com")
                .orElseGet(() -> {
                    User devUser = User.builder()
                            .email("teumteum@kakao.com")
                            .socialId(UUID.randomUUID().toString())
                            .socialType(SocialType.KAKAO)
                            .build();
                    return userRepository.save(devUser);
                });
    }


    // Resolver 사용자 조회
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }


    // 온보딩 - 약관 동의
    @Override
    @Transactional
    public void saveAgreement(UserRequestDTO.AgreeRequest request, User user) {
        // 1. 기존 동의 이력이 있는지 확인
        if (agreementRepository.existsByUser(user)) {
            throw new UserHandler(UserErrorStatus.AGREEMENT_ALREADY_EXISTS);
        }

        // 2. 필수 항목 동의 여부 확인
        if (!request.getTosConsent()) {
            throw new UserHandler(UserErrorStatus.TOS_CONSENT_NOT_AGREED);
        }
        if (!request.getPrivacyConsent()) {
            throw new UserHandler(UserErrorStatus.PRIVACY_CONSENT_NOT_AGREED);
        }

        // 3. Entity 변환
        Agreement agreement = AgreementConverter.toAgreement(request, user);

        // 4. 저장
        agreementRepository.save(agreement);

        // 5. 사용자 step 변경
        user.updateStep(UserStep.ONBOARDING);
    }


    // 온보딩 - 닉네임 & 분야/직종 등록
    @Override
    @Transactional
    public void saveNicknameAndJob(UserRequestDTO.NicknameJobRequest request, User user) {
        // 1. 닉네임 중복 여부 확인
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

        // 2. 닉네임과 분야/직종 수정
        user.updateNicknameAndJob(request.getNickname(), request.getJobField());
    }
}
