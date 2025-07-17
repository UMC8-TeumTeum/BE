package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.AgreementRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;

    @Override
    public Long searchByNickname(String nickname, Long requesterId) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));

        if (user.getId().equals(requesterId)) {
            throw new GeneralException(UserErrorStatus.USER_NOT_FOUND);
        }

        return user.getId();
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

    // 소셜 로그인 시, 사용자 다음 화면 결정
    @Override
    public String determineUserNextStep(User user) {
        // 1. 약관 동의 체크 -> 약관 동의 화면
        Agreement agreement = agreementRepository.findByUser(user)
                .orElse(null);
        if (agreement == null) {
            return "AGREEMENT";
        }

        // 2. 닉네임 체크 -> 온보딩 화면
        if (user.getNickname() == null) {
            return "ONBOARDING";
        }

        // 3. 메인 화면
        return "MAIN";
    }
}
