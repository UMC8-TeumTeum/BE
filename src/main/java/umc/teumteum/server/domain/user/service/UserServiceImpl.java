package umc.teumteum.server.domain.user.service;

import static umc.teumteum.server.domain.user.util.ImageConstants.ALLOWED_IMAGE_TYPES;
import static umc.teumteum.server.domain.user.util.ImageConstants.DEFAULT_IMAGE;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.home.converter.ScheduleConverter;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.RoutineStatus;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.converter.OnboardingConverter;
import umc.teumteum.server.domain.user.converter.UserConverter;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.dto.UserRequestDto;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.NotificationSetting;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.exception.OnboardingException;
import umc.teumteum.server.domain.user.exception.UserException;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.NotificationSettingRepository;
import umc.teumteum.server.domain.user.repository.RemindAlarmJdbcRepository;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.domain.user.util.UserDataCleaner;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.dto.TimeRange;
import umc.teumteum.server.global.jwt.JwtProvider;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.util.TimeUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final S3Util s3Util;
    private final JwtProvider jwtProvider;
    private final RoutineRepository routineRepository;
    private final TimeUtil timeUtil;
    private final ScheduleRepository scheduleRepository;
    private final RemindAlarmRepository remindAlarmRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;
    private final RemindAlarmJdbcRepository remindAlarmJdbcRepository;
    private final UserDataCleaner userDataCleaner;

    @Resource(name = "profileImageRedisTemplate")
    private RedisTemplate<String, String> profileImageRedisTemplate;

    private static final Set<Integer> ALLOWED_REMIND_ALARM_VALUES = Set.of(1, 3, 5, 10, 30);

    @Override
    public List<UserSearchResponseDto> searchUsersByKeyword(String keyword, Long userId) {
        String keywordLower = keyword.toLowerCase();
        LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();

        Comparator<Map.Entry<User, Integer>> byDistance = Comparator.comparingInt(Map.Entry::getValue);

        return userRepository.searchUserWithBlockCheck(keyword, userId).stream()
                .filter(u -> u.getStatus() != UserStatus.INACTIVE)
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
                // 이미 존재하는 경우
                .map(user -> {
                    if (user.getStatus() == UserStatus.INACTIVE) {
                        throw new UserException(ErrorStatus.INACTIVE_USER);
                    }
                    return user;
                })
                // 존재하지 않는 경우
                .orElseGet(() -> {
                    // 1) User 생성
                    User newUser = User.builder()
                            .socialType(socialType)
                            .socialId(socialId)
                            .email(email)
                            .build();
                    User savedUser = userRepository.save(newUser);

                    // 2) NotificationSetting 생성
                    NotificationSetting notificationSetting = NotificationSetting.builder()
                            .user(savedUser)
                            .teum(true)
                            .follow(true)
                            .todayTodo(true)
                            .remindAlarm(true)
                            .build();
                    notificationSettingRepository.save(notificationSetting);

                    return savedUser;
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

    // 마이페이지 - 알림 설정 변경
    @Transactional
    @Override
    public void updateAlarm(UserRequestDto.NotificationSettingRequest request, User user) {
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new UserException(UserErrorStatus.NOTIFICATION_NOT_FOUND));

        setting.update(
                request.getTodayTodo(),
                request.getRemindAlarm(),
                request.getTeum(), request.getFollow()
        );
    }

    // 마이페이지 - 반복일정 조회
    @Override
    public List<UserResponseDTO.RoutineDTO> getRoutines(Weekday weekday, User user) {
        // 1. 유저 조회
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(()-> new UserException(UserErrorStatus.USER_NOT_FOUND));

        // 2. 요일별 반복일정 조회
        List<Routine> routines = routineRepository.findByUserAndWeekday(existingUser, weekday);

        // 3. 응답 변환
        return routines.stream()
                .map(UserConverter::toRoutineDTO)
                .toList();
    }

    // 마이페이지 - 반복일정 삭제
    @Transactional
    @Override
    public void deleteRoutine(Long routineId) {
        // 1. 루틴 존재 여부 확인
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new UserException(UserErrorStatus.ROUTINE_NOT_FOUND));

        // 2. 오늘 & 미래 스케줄 조회
        LocalDate today = LocalDate.now();
        List<Schedule> schedules = scheduleRepository.findByRoutineAndDateGreaterThanEqual(routine, today);

        // 3. 스케줄 리마인드 삭제
        for (Schedule schedule : schedules) {
            scheduleReminderRepository.deleteByScheduleId(schedule.getId());
        }

        // 4. 스케줄 삭제
        scheduleRepository.deleteAll(schedules);

        // 5. 과거 스케줄의 연관관계 제거
        List<Schedule> pastSchedules = scheduleRepository.findByRoutineId(routineId);
        pastSchedules.forEach(s -> {
            s.setRoutine(null);
        });
        scheduleRepository.saveAll(pastSchedules);

        // 6. 루틴 삭제
        routineRepository.delete(routine);
    }

    // 마이페이지 - 반복일정 등록
    @Transactional
    @Override
    public void saveRoutine(OnboardingRequestDto.RoutineDTO request, User user) {

        // 1. 단일 일정 내에서 시작 & 종료시간 확인
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        validateRoutineTimeRange(startTime, endTime);

        // 2. 기존 존재하는 루틴과 충돌이 없는지 여부 검토
        List<Routine> existingRoutines = routineRepository.findByUserAndWeekday(user, request.getWeekday());
        validateExistingRoutineConflicts(request, existingRoutines);

        // 3. 저장
        Routine routine = OnboardingConverter.toRoutine(request, user);
        routineRepository.save(routine);

        // 4. 해당 날짜가 오늘이라면 스케줄 테이블에 삽입
        Weekday todayWeekday = Weekday.from(LocalDate.now().getDayOfWeek());
        LocalDate today = LocalDate.now();

        if(request.getWeekday().equals(todayWeekday)){
            Schedule schedule = ScheduleConverter.routineToSchedule(routine,today, RoutineStatus.ORIGINAL);
            Schedule savedSchedule = scheduleRepository.save(schedule);

            // 4-1. 리마인드 알림 조회
            List<RemindAlarm> remindAlarms = remindAlarmRepository.findAllByUser(user);

            // 4-2. 스케줄 리마인드 생성
            List<ScheduleReminder> scheduleReminder = ScheduleConverter.remindAlarmToScheduleReminders(savedSchedule, remindAlarms, AlarmStatus.ACTIVE);

            // 4-3. 저장
            scheduleReminderRepository.saveAll(scheduleReminder);
        }
    }

    // 마이페이지 -  반복일정 수정
    @Transactional
    @Override
    public void updateRoutine(Long routineId, OnboardingRequestDto.RoutineDTO request, User user) {
        // 1. 루틴 존재 여부 확인
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new UserException(UserErrorStatus.ROUTINE_NOT_FOUND));

        // 2. 단일 일정 내에서 시작 & 종료시간 확인
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        validateRoutineTimeRange(startTime, endTime);

        // 3. 기존 루틴들과의 시간 충돌 여부 검사
        // 3-1. 요일과 유저로 루틴 조회
        List<Routine> existingRoutines = routineRepository.findByUserAndWeekday(user, request.getWeekday());

        // 3-2. 현재 수정 중인 루틴 (ID 일치)을 제외하고 필터링
        List<Routine> routinesToCheck = existingRoutines.stream()
                .filter(r -> !r.getId().equals(routineId))
                .collect(Collectors.toList());

        validateExistingRoutineConflicts(request, routinesToCheck);

        // 4. 반복일정 필드 수정
        routine.updateField(
                request.getTitle(),
                request.getDescription(),
                request.getWeekday(),
                request.getStartTime(),
                request.getEndTime()
        );

        // 5. 해당 날짜가 오늘이라면 오늘 이후의 스케줄 수정
        Weekday todayWeekday = Weekday.from(LocalDate.now().getDayOfWeek());
        LocalDate today = LocalDate.now();

        if(request.getWeekday().equals(todayWeekday)){
            // 5-1. 오늘 이후의 루틴 ID가 같은 스케줄 조회
            List<Schedule> scheduleList = scheduleRepository.findByRoutineAndDateGreaterThanEqual(routine, today);

            // 5-2. 기존 스케줄에서 필드 수정
            scheduleList.forEach(schedule -> {
                schedule.updateFromRoutine(routine);
            });
        }
    }

    // 단일 일정 내에서 시작 & 종료시간 확인
    private void validateRoutineTimeRange(LocalTime startTime, LocalTime endTime) {
        // 1. 종료시간 00:00의 경우 무조건 허용 (=다음날 자정에 종료를 의미)
        if (endTime.equals(LocalTime.MIDNIGHT)) {
            return;
        }

        // 2. 기본 유효성 검증 (시작시간 < 종료시간)
        if (!startTime.isBefore(endTime)) {
            // ex) 13:00~03:00, 10:00~10:00 등이 해당
            throw new OnboardingException(UserErrorStatus.INVALID_TIME_RANGE);
        }
    }

    // 기존 루틴들과의 시간 충돌 여부 검사
    private void validateExistingRoutineConflicts(
            OnboardingRequestDto.RoutineDTO newRoutineRequest,
            List<Routine> existingRoutines
    ){
        // 1. 기존 루틴이 없으면 검증 통과
        if(existingRoutines.isEmpty()){
            return;
        }

        // 2. 기존 루틴들과 새 루틴을 모두 TimeRange 리스트로 변환
        List<TimeRange> timeRanges = new ArrayList<>();

        // 2-1. 기존 루틴들을 TimeRange로 변환하여 추가 (Routine::toTimeRange 필요)
        existingRoutines.stream()
                .map(TimeRange::from)
                .forEach(timeRanges::add);

        // 2-2. 새로 등록한 루틴 TimeRange로 변환 후 추가
        timeRanges.add(TimeRange.from(newRoutineRequest));

        // 2-3. 충돌 여부 검증
        timeUtil.validateTimeRangeConflicts(timeRanges,UserErrorStatus.ROUTINE_TIME_CONFLICT);
    }

    // 마이페이지 - 수면패턴 수정
    @Transactional
    @Override
    public void updateSleepPattern(OnboardingRequestDto.SleepPatternRequest request, User user) {
        // 1. 수면패턴 검증 (최대 23시간)
        OnboardingServiceImpl.validateSleepPattern(request.getSleepTime(), request.getWakeTime());

        // 2. 수면패턴 업데이트
        user.updateSleepPattern(request.getSleepTime(), request.getWakeTime());
    }

    // 마이페이지 - 리마인드 알림 설정 조회
    @Override
    public UserResponseDTO.RemindAlarmList getReminders(User user) {
        List<Integer> remindAlarms = remindAlarmRepository.findAllByUser(user)
                .stream()
                .map(RemindAlarm::getMinutesBefore)
                .sorted()
                .toList();

        return UserResponseDTO.RemindAlarmList.builder()
                .remindAlarms(remindAlarms)
                .build();
    }

    // 마이페이지 - 리마인드 알림 수정
    @Transactional
    @Override
    public void updateReminders(OnboardingRequestDto.RemindAlarmList request, User user) {
        // 1. 기존 등록된 RemindAlarm 조회
        List<RemindAlarm> remindAlarms = remindAlarmRepository.findAllByUser(user);

        // 2. 유효성 검증 :알림 설정 범위 확인 (1, 3, 5, 10, 30)
        if (!ALLOWED_REMIND_ALARM_VALUES.containsAll(request.getRemindAlarms())) {
            throw new OnboardingException(UserErrorStatus.INVALID_REMIND_ALARM_VALUE);
        }

        // 3. 요청 값과 비교해 추가 & 삭제할 값 고르기
        // 기존 값들 (minutesBefore만 추출)
        Set<Integer> currentSet = remindAlarms.stream()
                .map(RemindAlarm::getMinutesBefore)
                .collect(Collectors.toSet());

        // 요청 값들
        Set<Integer> requestSet = new HashSet<>(request.getRemindAlarms());

        // diff 계산
        Set<Integer> toAdd = requestSet.stream()
                .filter(v-> !currentSet.contains(v))
                .collect(Collectors.toSet());

        Set<Integer> toRemove = currentSet.stream()
                .filter(v-> !requestSet.contains(v))
                .collect(Collectors.toSet());

        // 4. 리마인드 알림 삭제 처리
        if (!toRemove.isEmpty()) {
            remindAlarmRepository.deleteByUserAndMinutesBeforeIn(user, toRemove);
        }

        // 5. 리마인드 알림 추가 처리
        if (!toAdd.isEmpty()) {
            List<RemindAlarm> alarmsToAdd = OnboardingConverter.toRemindAlarmList(new ArrayList<>(toAdd), user);
            remindAlarmJdbcRepository.batchInsertRemindAlarms(alarmsToAdd);
        }
    }

    // 회원탈퇴
    @Transactional
    @Override
    public void deleteUser(User user) {
        User u = userRepository.findById(user.getId())
                .orElseThrow(()-> new UserException(UserErrorStatus.USER_NOT_FOUND));

        // 1. 프로필 이미지 객체 삭제
        deleteUserImage(u);

        // 2. 유저 익명화
        u.withdraw();

        // 3. 유저 관련 데이터 삭제
        userDataCleaner.clean(u.getId());
    }

    // 수면패턴 삭제
    @Transactional
    @Override
    public void deleteSleepPattern(User user) {
        User u = userRepository.findById(user.getId())
                .orElseThrow(()-> new UserException(UserErrorStatus.USER_NOT_FOUND));

        u.updateSleepPattern(null, null);
    }

    // 마이페이지 - 소셜 계정 정보 조회
    @Transactional(readOnly = true)
    @Override
    public UserResponseDTO.AccountInfoDTO getAccountInfo(User user) {
        return UserConverter.toAccountInfoDTO(user);
    }

    // 마이페이지 - 공개 투두 조회
    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDTO.TodoDTO> getPublicTodo(User user) {

        // 1. 스케줄 조회
        LocalDate now = LocalDate.now();
        List<Schedule> schedulesList = scheduleRepository.findByUserAndDateAndIsPublicAndRoutineStatus(user.getId(), now);

        // 2. 시작시간 기준으로 정렬 후 상위 2개 반환
        return schedulesList.stream()
                .sorted(Comparator.comparing(Schedule::getStartTime))
                .limit(2)
                .map(UserConverter::toTodoDTO)
                .toList();
    }


    /**
     * 유저 프로필 삭제 헬퍼 메서드
     */
    public void deleteUserImage(User user) {

        String oldFileName = user.getProfileImageName();

        // 1. default 이미지 -> 삭제하지 않고 통과
        if(oldFileName == null || oldFileName.equals(DEFAULT_IMAGE)){
            return;
        }

        // 2. 기존 객체 삭제
        try{
            s3Util.deleteObject("profile/" + oldFileName);
        } catch(Exception e){
            log.warn("S3 profile delete failed. userId={}, file={}", user.getId(), oldFileName, e);
        }

        // 3. DB 저장 키 교체 (S3 Key -> default)
        user.updateProfileImageName(DEFAULT_IMAGE);
    }
}
