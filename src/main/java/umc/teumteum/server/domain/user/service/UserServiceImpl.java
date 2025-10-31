package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.converter.UserConverter;
import umc.teumteum.server.domain.user.dto.UserRequestDto;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.exception.UserException;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final S3Util s3Util;

    @Override
    public List<UserSearchResponseDto> searchUsersByKeyword(String keyword, Long userId) {
        String keywordLower = keyword.toLowerCase();
        LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();

        Comparator<Map.Entry<User, Integer>> byDistance = Comparator.comparingInt(Map.Entry::getValue);

        return userRepository.findByNicknameContaining(keyword).stream()
                .filter(user -> !user.getId().equals(userId))
                .map(user -> Map.entry(user,
                        distanceCalculator.apply(keywordLower, user.getNickname().toLowerCase())))
                .sorted(byDistance)
                .limit(5)
                .map(entry -> userConverter.toSearchResponseDto(entry.getKey()))
                .toList();
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


    @Override
    public UserResponseDTO.MyPageDTO getMyPage(User user) {
        String profileImageUrl = s3Util.toPresignedUrl("profile/" + user.getProfileImageName(), Duration.ofMinutes(30));
        return UserConverter.toMyPageDTO(user, profileImageUrl);
    }

    // 마이페이지 - 개인정보 수정
    @Transactional
    @Override
    public void updateProfile(UserRequestDto.ProfileRequest request, User user) {
        // 1. 유저 조회
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(()-> new UserException(UserErrorStatus.USER_NOT_FOUND));

        // 2. 닉네임 수정 시, 닉네임 중복 검증
        if (!Objects.equals(existingUser.getNickname(), request.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new UserException(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
        }

        // 3. 프로필 수정
        existingUser.updateProfile(
                request.getNickname(),
                request.getJobField(),
                request.getTimePublic()
        );
    }
}
