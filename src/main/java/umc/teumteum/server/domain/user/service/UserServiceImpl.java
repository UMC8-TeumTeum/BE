package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.converter.UserConverter;
import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.repository.AgreementRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;

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
