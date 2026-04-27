package umc.teumteum.server.domain.friend.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.friend.converter.FriendConverter;
import umc.teumteum.server.domain.friend.dto.FriendResponseDto;
import umc.teumteum.server.domain.friend.dto.MutualFriendProjection;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.friend.exception.FriendException;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.BlockRepository;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.notification.service.NotificationUseCases;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.exception.UserException;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.dto.PagingResponseDto;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ScheduleRepository scheduleRepository;
    private final BlockRepository blockRepository;

    private final FriendConverter friendConverter;
    private final NotificationUseCases notificationUseCases;

    private final S3Util s3Util;

    @Resource(name = "profileImageRedisTemplate")
    private RedisTemplate<String, String> profileImageRedisTemplate;

    private static final String PROFILE_URL_CACHE_KEY_PREFIX = "profile:url:";
    private static final Duration PROFILE_URL_CACHE_TTL = Duration.ofMinutes(20);


    private static final Set<ScheduleType> GENERAL_SCHEDULE_TYPES =
            EnumSet.of(ScheduleType.TODO, ScheduleType.WISH, ScheduleType.AI);

    private static final Set<ScheduleStatus> TEUM_VALID_STATUSES =
            EnumSet.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED);

    private String toProfileUrl(User user) {
        if (user == null || user.getProfileImageName() == null) {
            return null;
        }
        return s3Util.toPresignedUrl("profile/" + user.getProfileImageName(), Duration.ofMinutes(30));
    }

    @Override
    @Transactional
    public void follow(User loginUser, Long targetUserId) {
        // 1. 자기 자신을 팔로우하는지 확인
        validateNotSelf(loginUser.getId(), targetUserId);

        // 2. 상대방 조회
        User targetUser = getUserOrThrow(targetUserId);

        // 3. 이미 팔로우 중인지 확인
        if (friendRepository.existsByFollowerAndFollowing(loginUser, targetUser)) {
            throw new FriendException(FriendErrorStatus.ALREADY_FOLLOWING);
        }

        // 4. 차단 관계 확인 (내가 차단했거나, 상대방이 나를 차단했으면 팔로우 불가)
        validateBlockRelationship(loginUser, targetUser);

        // 5. Friend 생성 및 저장
        Friend friend = FriendConverter.toFriend(loginUser, targetUser);
        friendRepository.save(friend);

        // 5. 알림 전송
        notificationUseCases.notifyFollow(loginUser, targetUser, friend.getId());
    }

    @Override
    @Transactional
    public void unfollow(User loginUser, Long targetUserId) {
        // 1. 자기 자신을 언팔로우하는지 확인
        validateNotSelf(loginUser.getId(), targetUserId);

        // 2. 상대방 조회
        User targetUser = getUserOrThrow(targetUserId);

        // 3. 팔로우 관계가 존재하는지 확인
        Friend friend = friendRepository.findByFollowerAndFollowing(loginUser, targetUser)
                .orElseThrow(() -> new FriendException(FriendErrorStatus.NOT_FOLLOWING));

        // 4. Friend 삭제
        friendRepository.delete(friend);
    }


    @Override
    @Transactional(readOnly = true)
    public PagingResponseDto<FriendResponseDto.MutualFriend> getMutualFriends(User loginUser, Long excludeUserId,
                                                                              int page, int size) {
        // 1. 자기 자신을 제외하는지 확인
        validateNotSelf(loginUser.getId(), excludeUserId);

        // 2. 제외하려는 대상 존재 여부 확인
        validateUserExists(excludeUserId);

        // 3. Pageable 생성 (정렬은 쿼리 내 ORDER BY로 처리)
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);

        // 4. 맞팔로우 관계 조회 (DTO Projection)
        Slice<MutualFriendProjection> mutualFriendsSlice = friendRepository.findMutualFriendsExcluding(
                loginUser, excludeUserId, UserStatus.ACTIVE, pageable);

        // 5. 프로필 이미지 URL 변환 및 Dto 변환
        List<MutualFriendProjection> projections = mutualFriendsSlice.getContent();
        Map<String, String> profileUrlMap = getOrLoadProfileImageUrls(projections);

        List<FriendResponseDto.MutualFriend> mutualFriendList = projections.stream()
                .map(projection -> FriendConverter.toMutualFriendDto(
                        projection,
                        profileUrlMap.get(projection.getProfileImageName())))
                .toList();

        // 6. PagingResponseDto 생성
        return new PagingResponseDto<>(mutualFriendList, mutualFriendsSlice.hasNext());
    }

    @Override
    @Transactional
    public FriendResponseDto.FriendFavorite updateFavorite(User loginUser, Long targetUserId, Boolean isFavorite) {
        // 1. 자기 자신을 즐겨찾기하는지 확인
        validateNotSelf(loginUser.getId(), targetUserId);

        // 2. 상대방 조회
        User targetUser = getUserOrThrow(targetUserId);

        // 3. 팔로잉 관계 조회
        Friend friend = friendRepository.findByFollowerAndFollowing(loginUser, targetUser)
                .orElseThrow(() -> new FriendException(FriendErrorStatus.NOT_FOLLOWING));

        // 4. 즐겨찾기 상태 수정
        if (friend.getIsFavorite().equals(isFavorite)) {
            throw new FriendException(FriendErrorStatus.FAVORITE_ALREADY_SET);
        }
        friend.updateIsFavorite(isFavorite);

        // 5. 결과 반환
        return FriendConverter.toFriendFavoriteDto(targetUser, friend.getIsFavorite());
    }

    // 사용자가 팔로우한 유저 목록을 정렬 후 페이징하여 반환
    @Override
    @Transactional(readOnly = true)
    public PagingResponseDto<FriendResponseDto.FollowingFriend> getFollowingsByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Order.desc("isFavorite"), Sort.Order.asc("following.nickname"))
        );

        Slice<Friend> slice = friendRepository.findByFollowerId(userId, UserStatus.ACTIVE, pageable);

        List<FriendResponseDto.FollowingFriend> dtoList = slice.getContent().stream()
                .map(friend -> {
                    String url = toProfileUrl(friend.getFollowing());
                    return friendConverter.toFollowingUserResponse(friend, url);
                })
                .collect(Collectors.toList());

        return new PagingResponseDto<>(dtoList, slice.hasNext());
    }

    // 사용자를 팔로우한 유저 목록을 정렬 후 페이징하여 반환
    @Override
    @Transactional(readOnly = true)
    public PagingResponseDto<FriendResponseDto.FollowerFriend> getFollowersByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by("follower.nickname").ascending()
        );

        Slice<Friend> slice = friendRepository.findByFollowingId(userId, UserStatus.ACTIVE, pageable);

        List<FriendResponseDto.FollowerFriend> dtoList = slice.getContent().stream()
                .map(friend -> {
                    String url = toProfileUrl(friend.getFollower());
                    return friendConverter.toFollowerUserResponse(friend, url);
                })
                .collect(Collectors.toList());

        return new PagingResponseDto<>(dtoList, slice.hasNext());
    }

    @Override
    public FriendResponseDto.FriendProfile getFriendProfile(Long loginUserId, Long targetUserId) {
        validateNotSelf(loginUserId, targetUserId);

        // 차단 검증을 위해 loginUser도 조회
        User loginUser = getUserOrThrow(loginUserId);
        User targetUser = getUserOrThrow(targetUserId);

        // 차단 관계 검증
        validateBlockRelationship(loginUser, targetUser);

        Optional<Friend> followRelationOpt =
                friendRepository.findByFollowerIdAndFollowingId(loginUserId, targetUser.getId());

        String url = toProfileUrl(targetUser);
        return friendConverter.toFriendProfileResponse(targetUser, followRelationOpt.orElse(null), url);
    }

    @Override
    public FriendResponseDto.FriendTeumTime getFriendTeumTime(Long loginUserId, Long targetUserId) {
        validateNotSelf(loginUserId, targetUserId);

        User loginUser = getUserOrThrow(loginUserId);
        User targetUser = getUserOrThrow(targetUserId);

        // 차단 관계 검증
        validateBlockRelationship(loginUser, targetUser);

        List<ScheduleType> targetTypes = List.of(
                ScheduleType.AI,
                ScheduleType.WISH,
                ScheduleType.TODO,
                ScheduleType.TEUM
        );

        List<Schedule> rawSchedules = scheduleRepository.findSchedulesForTeumTime(
                targetUserId,
                LocalDateTime.now(),
                targetTypes
        );

        List<Schedule> filtered = rawSchedules.stream()
                .filter(s ->
                        (s.getType() == ScheduleType.TEUM && s.getStatus() == ScheduleStatus.COMPLETED) ||
                                (s.getType() != ScheduleType.TEUM && s.getStatus() == ScheduleStatus.ACTIVE)
                )
                .collect(Collectors.toList());

        long totalMinutes = friendConverter.calculateTeumTime(filtered);
        return friendConverter.toFriendTeumTimeResponse(totalMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto.FriendPublicTodo> getRecentPublicTodos(Long loginUserId, Long targetUserId) {
        validateNotSelf(loginUserId, targetUserId);

        User loginUser = getUserOrThrow(loginUserId);
        User targetUser = getUserOrThrow(targetUserId);

        // 차단 관계 검증
        validateBlockRelationship(loginUser, targetUser);

        List<Schedule> rawSchedules = scheduleRepository.findAllPublicByUserId(targetUserId);

        List<Schedule> filtered = rawSchedules.stream()
                .filter(s -> {
                    if (s.getType() == ScheduleType.TEUM) {
                        return s.getStatus() == ScheduleStatus.ACTIVE || s.getStatus() == ScheduleStatus.COMPLETED;
                    } else {
                        return s.getStatus() == ScheduleStatus.ACTIVE &&
                                GENERAL_SCHEDULE_TYPES.contains(s.getType());
                    }
                })
                .limit(2)
                .collect(Collectors.toList());

        return friendConverter.toFriendPublicTodoResponse(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto.FriendPublicTodo> getDailyPublicTodos(Long loginUserId, Long targetUserId,
                                                                        String date) {
        validateNotSelf(loginUserId, targetUserId);

        User loginUser = getUserOrThrow(loginUserId);
        User targetUser = getUserOrThrow(targetUserId);

        // 차단 관계 검증
        validateBlockRelationship(loginUser, targetUser);

        LocalDate localDate = LocalDate.parse(date);

        List<Schedule> schedules = scheduleRepository.findPublicSchedulesByUserAndDate(targetUserId, localDate);

        List<Schedule> filtered = schedules.stream()
                .filter(s -> {
                    if (s.getType() == ScheduleType.TEUM) {
                        return TEUM_VALID_STATUSES.contains(s.getStatus());
                    } else {
                        return s.getStatus() == ScheduleStatus.ACTIVE &&
                                GENERAL_SCHEDULE_TYPES.contains(s.getType());
                    }
                })
                .collect(Collectors.toList());

        return friendConverter.toFriendPublicTodoResponseList(filtered);
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getTodoDatesOfMonth(Long loginUserId, Long targetUserId, String month) {
        validateNotSelf(loginUserId, targetUserId);

        User loginUser = getUserOrThrow(loginUserId);
        User targetUser = getUserOrThrow(targetUserId);

        // 차단 관계 검증
        validateBlockRelationship(loginUser, targetUser);

        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // TODO/WISH/AI 일정 (status: ACTIVE)
        List<LocalDate> generalDates = scheduleRepository.findPublicActiveTodos(targetUserId, GENERAL_SCHEDULE_TYPES,
                start, end);

        // TEUM 일정 (status: ACTIVE, COMPLETED)
        List<LocalDate> teumDates = scheduleRepository.findPublicTeumDates(targetUserId, TEUM_VALID_STATUSES, start,
                end);

        List<LocalDate> allDates = Stream.concat(generalDates.stream(), teumDates.stream())
                .distinct()
                .sorted()
                .toList();

        return friendConverter.toDateStringList(allDates);
    }


    /**
     * 주어진 ID에 해당하는 User를 조회합니다. - User 객체 자체가 필요한 경우에 사용합니다. - 존재하지 않으면 예외를 던집니다.
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new FriendException(FriendErrorStatus.USER_NOT_FOUND));
    }

    /**
     * 주어진 ID에 해당하는 User가 존재하는지만 확인합니다. - 객체 자체가 필요하지 않고, 존재 여부만 확인할 때 사용합니다. - 존재하지 않으면 예외를 던집니다.
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new FriendException(FriendErrorStatus.USER_NOT_FOUND);
        }
    }

    private void validateNotSelf(Long loginUserId, Long targetUserId) {
        if (loginUserId.equals(targetUserId)) {
            throw new FriendException(FriendErrorStatus.INVALID_SELF_REQUEST);
        }
    }

    // 차단 관계 검증
    private void validateBlockRelationship(User user1, User user2) {
        if (blockRepository.existsByBlockerAndBlocked(user1, user2) ||
                blockRepository.existsByBlockerAndBlocked(user2, user1)) {
            throw new FriendException(FriendErrorStatus.BLOCK_ACTION_FORBIDDEN);
        }
    }

    private Map<String, String> getOrLoadProfileImageUrls(List<MutualFriendProjection> projections) {
        // 입력 자체가 없으면 바로 종료
        if (projections.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. projection에서 profileImageName만 추출
        List<String> profileImageNames = projections.stream()
                .map(MutualFriendProjection::getProfileImageName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // null 제거 후 아무것도 없으면 종료
        if (profileImageNames.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. Redis 조회를 위한 cache key 생성
        List<String> cacheKeys = profileImageNames.stream()
                .map(this::buildCacheKey)
                .toList();

        // 3. Redis multiGet으로 한번에 조회
        List<String> cachedUrls = profileImageRedisTemplate.opsForValue().multiGet(cacheKeys);

        // 최종 결과를 담을 Map (profileImageName -> URL)
        Map<String, String> profileImageUrlMap = new HashMap<>();
        // 캐시 miss 결과를 Redis에 한 번에 저장하기 위한 Map
        Map<String, String> missedCacheMap = new HashMap<>();

        // 4. 조회 결과를 순회하면서 캐시 hit/miss 처리
        for (int i = 0; i < profileImageNames.size(); i++) {

            String profileImageName = profileImageNames.get(i);
            String cachedUrl = cachedUrls.get(i);

            // 4-1. cache hit
            if (cachedUrl != null) {
                profileImageUrlMap.put(profileImageName, cachedUrl);
            }
            // 4-2. cache miss
            else {
                // Presigned URL 생성
                String newUrl = s3Util.toPresignedUrl("profile/" + profileImageName, Duration.ofMinutes(30));

                profileImageUrlMap.put(profileImageName, newUrl);
                missedCacheMap.put(buildCacheKey(profileImageName), newUrl);
            }
        }

        // 5. cache miss 데이터 Redis에 일괄 저장 (pipeline)
        if (!missedCacheMap.isEmpty()) {
            profileImageRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {

                StringRedisConnection src = (StringRedisConnection) connection;

                missedCacheMap.forEach((k, v) ->
                        src.set(k, v, Expiration.from(PROFILE_URL_CACHE_TTL), RedisStringCommands.SetOption.UPSERT));
                return null;
            });
        }

        // 6. profileImageName -> URL 매핑 결과 반환
        return profileImageUrlMap;
    }

    // cache key 생성
    private String buildCacheKey(String profileImageName) {
        return PROFILE_URL_CACHE_KEY_PREFIX + profileImageName;
    }
}
