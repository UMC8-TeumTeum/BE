package umc.teumteum.server.domain.user.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.converter.OnboardingConverter;
import umc.teumteum.server.domain.user.converter.UserConverter;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.exception.UserException;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.jwt.JwtProvider;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.util.*;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final S3Util s3Util;
    private final JwtProvider jwtProvider;

    @Resource(name = "profileImageRedisTemplate")
    private RedisTemplate<String, String> profileImageRedisTemplate;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/svg+xml"
    );

    private static final String DEFAULT_IMAGE = "default.svg";

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

    // 프로필 이미지 업로드, Presigned URL 발급
    @Override
    public OnboardingResponseDto.ProfileImagePresignedUrlResponse generateProfileImagePresignedUrl(
            HttpServletRequest httpServletRequest, OnboardingRequestDto.ProfileImagePresignedUrlRequest request, User user) {

        // 1. content-type 검증
        String contentType = request.getContentType().toLowerCase();
        if(!ALLOWED_IMAGE_TYPES.contains(contentType)){
            throw new UserException(UserErrorStatus.UNSUPPORTED_IMAGE_FORMAT);
        }

        // 2. 확장자 추출
        String extension = contentType.substring(contentType.lastIndexOf("/") + 1);
        // svg+xml의 경우 svg로 변환
        if ("svg+xml".equals(extension)) {
            extension = "svg";
        }

        // 3. 파일명 생성
        String fileName = UUID.randomUUID() + "." + extension;

        // 4. S3 Key 구성 (profile/{fileName})
        String key = "profile/" + fileName;

        // 5. Presigned URL 발급
        String presignedUrl = s3Util.toUploadPresignedUrl(key, contentType, Duration.ofMinutes(30));

        // 6. S3 업로드 예정인 파일이름 redis에 저장
        String userId = user.getId().toString();
        String sessionId = jwtProvider.getSessionIdFromToken(jwtProvider.resolveToken(httpServletRequest));
        String imageFileKey = getProfileImageKey(userId, sessionId);
        profileImageRedisTemplate.opsForValue().set(imageFileKey, fileName, Duration.ofMinutes(30));

        // 7. 응답 반환
        return OnboardingConverter.toProfileImagePresignedUrlResponse(presignedUrl, fileName);
    }

    // 프로필 이미지 키 get
    private String getProfileImageKey(String userId, String sessionId) {
        return String.format("PROFILE_IMAGE_FILE_NAME:%s:%s", userId, sessionId);
    }

    // 마이페이지 - 프로필 이미지 수정
    @Transactional
    @Override
    public void saveProfileImage(HttpServletRequest httpServletRequest, OnboardingRequestDto.ProfileImageRequest request, User user) {

        // 1. Redis 조회하여 비교
        String userId = user.getId().toString();
        String sessionId = jwtProvider.getSessionIdFromToken(jwtProvider.resolveToken(httpServletRequest));
        String imageFileKey = getProfileImageKey(userId, sessionId);

        // 2. 검증
        try{
            String storedImageFileName = profileImageRedisTemplate.opsForValue().get(imageFileKey);

            // 2-1. TTL 만료
            if(storedImageFileName == null){
                throw new UserException(UserErrorStatus.EXPIRED_UPLOAD_SESSION);
            }

            // 2-2. 요청 파일명 != Redis 파일명
            if (!Objects.equals(storedImageFileName, request.getFileName())) {
                throw new UserException(UserErrorStatus.INVALID_IMAGE_NAME);
            }

            // 2-3. 기존 객체 삭제
            String oldFileName = user.getProfileImageName();

            if(oldFileName != null && !oldFileName.isEmpty() && !oldFileName.equals(DEFAULT_IMAGE)){
                s3Util.deleteObject("profile/" + oldFileName);
            }

            // 2-4. DB 저장 키 수정 (사용자 프로필 이미지 이름 업데이트)
            user.updateProfileImageName(request.getFileName());
        } finally {
            // 3. Redis 키 삭제
            profileImageRedisTemplate.delete(imageFileKey);
        }
    }

    // 마이페이지 - 프로필 이미지 삭제
    @Transactional
    @Override
    public void deleteProfileImage(User user) {

        String oldFileName = user.getProfileImageName();

        // 1. default 이미지 삭제 예외처리
        if(oldFileName == null || oldFileName.equals(DEFAULT_IMAGE)){
            throw new UserException(UserErrorStatus.CANNOT_DELETE_DEFAULT_IMAGE);
        }

        // 2. 기존 객체 삭제
        s3Util.deleteObject("profile/" + oldFileName);

        // 3. DB 저장 키 교체 (S3 Key -> default)
        user.updateProfileImageName(DEFAULT_IMAGE);
    }
}
