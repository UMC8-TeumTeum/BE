package umc.teumteum.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.converter.OnboardingConverter;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.exception.OnboardingHandler;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.AgreementRepository;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.dto.TimeRange;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.util.TimeUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;
    private final RoutineRepository routineRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;
    private final RemindAlarmRepository remindAlarmRepository;

    private final TimeUtil timeUtil;
    private final S3Util s3Util;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/svg+xml"
    );

    private static final Set<Integer> ALLOWED_REMIND_ALARM_VALUES = Set.of(1, 3, 5, 10, 30);


    // 온보딩 - 약관 동의
    @Override
    @Transactional
    public void saveAgreements(OnboardingRequestDto.AgreeRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.AGREEMENT);

        // 2. 필수 항목 동의 여부 확인
        if (!request.getTosConsent()) {
            throw new OnboardingHandler(UserErrorStatus.TOS_CONSENT_NOT_AGREED);
        }
        if (!request.getPrivacyConsent()) {
            throw new OnboardingHandler(UserErrorStatus.PRIVACY_CONSENT_NOT_AGREED);
        }

        // 3. Entity 변환
        Agreement agreement = OnboardingConverter.toAgreement(request, user);

        // 4. 저장
        agreementRepository.save(agreement);

        // 5. 사용자 step 변경
        user.updateStep(UserStep.ONBOARDING);
    }


    // 온보딩 - 닉네임 & 분야/직종 등록
    @Override
    @Transactional
    public void saveNicknameAndJob(OnboardingRequestDto.NicknameJobRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. 닉네임 중복 여부 확인
        // 기존 닉네임이 null이면(=처음 닉네임 등록) 단순 중복 체크
        if (user.getNickname() == null) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new OnboardingHandler(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
            }
        }
        // 기존 닉네임이 있으면(=온보딩 중단으로 인한 닉네임 재등록) 본인 닉네임 이외와 중복 체크
        else {
            if (!request.getNickname().equals(user.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
                throw new OnboardingHandler(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
            }
        }

        // 3. 닉네임과 분야/직종 수정
        user.updateNicknameAndJob(request.getNickname(), request.getJobField());
    }


    // 온보딩 - 프로필 이미지 업로드용 URl 발급
    @Override
    public OnboardingResponseDto.ProfileImagePresignedUrlResponse generateProfileImagePresignedUrl(OnboardingRequestDto.ProfileImagePresignedUrlRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. Content-Type 검증
        String contentType = request.getContentType().toLowerCase();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new OnboardingHandler(UserErrorStatus.UNSUPPORTED_IMAGE_FORMAT);
        }

        // 3. 확장자 추출
        String extension = contentType.substring(contentType.lastIndexOf("/") + 1);
        // svg+xml의 경우 svg로 변환
        if ("svg+xml".equals(extension)) {
            extension = "svg";
        }

        // 4. 파일명(UUID) 생성
        String fileName = UUID.randomUUID() + "." + extension;

        // 5. S3 Key 구성 (profile/{fileName})
        String key = "profile/" + fileName;

        // 6. Presigned URL 생성
        String presignedUrl = s3Util.toUploadPresignedUrl(key, contentType, Duration.ofMinutes(30));

        // 7. TODO S3 업로드 예정인 파일이름 REDIS에 저장

        // 8. 응답 DTO 반환
        return OnboardingConverter.toProfileImagePresignedUrlResponse(presignedUrl, fileName);
    }


    // 온보딩 - 프로필 이미지 등록
    @Override
    @Transactional
    public void saveProfileImage(OnboardingRequestDto.ProfileImageRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. TODO REDIS 조회하여 비교
        // throw new OnboardingHandler(UserErrorStatus.INVALID_IMAGE_NAME);

        // 2. 사용자 프로필 이미지 이름 업데이트
        user.updateProfileImageName(request.getFileName());
    }


    // 온보딩 - 수면패턴 등록
    @Override
    @Transactional
    public void saveSleepPattern(OnboardingRequestDto.SleepPatternRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. 수면패턴 수정
        user.updateSleepPattern(request.getSleepTime(), request.getWakeTime());
    }


    // 온보딩 - 반복일정 등록
    @Override
    @Transactional
    public void saveRoutines(OnboardingRequestDto.RoutineListRequest request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. 단일 일정 내에서 시작&종료시간 확인
        List<OnboardingRequestDto.RoutineDTO> routines = request.getRoutine();
        routines.forEach(this::validateSingleRoutineTimeRange);

        // 3. 요일별로 그룹핑 (EnumMap 사용)
        Map<Weekday, List<OnboardingRequestDto.RoutineDTO>> routinesByDay =
                routines.stream()
                        .collect(Collectors.groupingBy(
                                OnboardingRequestDto.RoutineDTO::getWeekday,
                                () -> new EnumMap<>(Weekday.class),
                                Collectors.toList()
                        ));

        // 4. 반복 일정끼리의 충돌 확인
        validateRoutineConflictsByDay(routinesByDay);

        // 5. 수면패턴과의 충돌 확인
        validateSleepPatternConflictsByDay(routinesByDay, user);

        // 6. 반복 일정 저장
        List<Routine> newRoutines = OnboardingConverter.toRoutineList(request.getRoutine(), user);
        routineRepository.saveAll(newRoutines);

        // 7. 오늘 요일의 일정은 스케줄에 추가
        List<Schedule> routineSchedules = OnboardingConverter.toScheduleList(newRoutines, user, LocalDate.now());
        scheduleRepository.saveAll(routineSchedules);
    }



    // 온보딩 - 리마인드 알림 설정 등록
    @Override
    @Transactional
    public void saveRemindAlarms(OnboardingRequestDto.RemindAlarmList request, User user) {
        // 1. 사용자 step 확인
        validateOnboardingStep(user, UserStep.ONBOARDING);

        // 2. 입력값 있는지 확인 (리마인드 알림 등록은 선택 입력)
        // (※ 온보딩 완료를 판단해야 하기 때문에 선택 입력임에도 API 호출이 필요)
        if (!request.getRemindAlarms().isEmpty()) {

            // 3. 알림 설정 범위 확인 (1, 3, 5, 10, 30)
            if (!ALLOWED_REMIND_ALARM_VALUES.containsAll(request.getRemindAlarms())) {
                throw new OnboardingHandler(UserErrorStatus.INVALID_REMIND_ALARM_VALUE);
            }

            // 4. RemindAlarm 저장
            List<RemindAlarm> remindAlarms = OnboardingConverter.toRemindAlarmList(request.getRemindAlarms(), user);
            remindAlarmRepository.saveAll(remindAlarms);

            // 5. 저장된 Schedule이 있으면 (반복일정 등록은 선택 입력)
            List<Schedule> existingSchedules = scheduleRepository.findByUser(user);
            if (!existingSchedules.isEmpty()) {

                // 6. 각 Schedule마다 ScheduleReminder 저장
                List<ScheduleReminder> scheduleReminders = OnboardingConverter.toScheduleReminderList(
                        request.getRemindAlarms(), existingSchedules);
                scheduleReminderRepository.saveAll(scheduleReminders);
            }
        }

        // 7. 최종 온보딩 완료로 사용자 step 변경
        user.updateStep(UserStep.MAIN);
    }




    // 사용자의 step을 확인
    private void validateOnboardingStep(User user, UserStep expectedStep) {
        if (user.getStep() == null || !user.getStep().equals(expectedStep)) {
            throw new OnboardingHandler(UserErrorStatus.INVALID_STEP);
        }
    }


    // 단일 일정의 시작시간과 종료시간이 올바른 범위인지 검증
    private void validateSingleRoutineTimeRange(OnboardingRequestDto.RoutineDTO routine) {
        LocalTime startTime = routine.getStartTime();
        LocalTime endTime = routine.getEndTime();

        // 1. 종료시간 00:00의 경우 무조건 허용 (=다음날 자정에 종료를 의미)
        if (endTime.equals(LocalTime.MIDNIGHT)) {
            return;
        }

        // 2. 기본 유효성 검증 (시작시간 < 종료시간)
        if (!startTime.isBefore(endTime)) {
            // ex) 13:00~03:00, 10:00~10:00 등이 해당
            throw new OnboardingHandler(UserErrorStatus.INVALID_TIME_RANGE);
        }
    }


    // 반복일정끼리의 충돌 확인
    private void validateRoutineConflictsByDay(Map<Weekday, List<OnboardingRequestDto.RoutineDTO>> routinesByDay) {
        routinesByDay.values().stream()
                // 1. 반복일정 2개 이상일 때만 충돌 검증
                .filter(dayRoutines -> dayRoutines.size() > 1)
                .forEach(dayRoutines -> {

                    // 2. 반복일정을 TimeRange로 변환
                    List<TimeRange> timeRanges = dayRoutines.stream()
                            .map(TimeRange::from)
                            .collect(Collectors.toList());

                    timeUtil.validateTimeRangeConflicts(timeRanges, UserErrorStatus.ROUTINE_TIME_CONFLICT);
                });
    }


    // 수면패턴과 반복일정 간의 충돌 확인
    private void validateSleepPatternConflictsByDay(Map<Weekday, List<OnboardingRequestDto.RoutineDTO>> routinesByDay, User user) {
        // 1. 수면패턴 저장 여부 확인 (선택입력이기 때문)
        if (user.getSleepTime() == null && user.getWakeTime() == null) {
            return;
        }

        // 2. 수면패턴을 TimeRange로 변환
        List<TimeRange> sleepTimeRanges = getSleepTimeRanges(user.getSleepTime(), user.getWakeTime());

        // 3. 반복일정과 수면패턴 시간 충돌 검증
        routinesByDay.values().stream()
                .map(dayRoutines -> {

                    List<TimeRange> allTimeRanges = new ArrayList<>();

                    // 반복일정 추가
                    allTimeRanges.addAll(dayRoutines.stream()
                            .map(TimeRange::from)
                            .toList());

                    // 수면패턴 추가
                    allTimeRanges.addAll(sleepTimeRanges);

                    return allTimeRanges;
                })
                .forEach(allTimeRanges ->
                        timeUtil.validateTimeRangeConflicts(allTimeRanges, UserErrorStatus.ROUTINE_SLEEP_CONFLICT));
    }


    // 수면패턴을 TimeRange 리스트로 변환
    private List<TimeRange> getSleepTimeRanges(LocalTime sleepTime, LocalTime wakeTime) {
        // 1. 다음 날까지 이어지는 수면 (ex. 22:00~08:00)
        if (sleepTime.isAfter(wakeTime) && !wakeTime.equals(LocalTime.MIDNIGHT)) {
            return List.of(
                    TimeRange.of(sleepTime, LocalTime.MIDNIGHT),
                    TimeRange.of(LocalTime.MIDNIGHT, wakeTime)
            );
        }

        // 2. 같은 날 안에서 끝나는 수면 (ex. 06:00~14:00, 18:00~00:00, 00:00~00:00)
        return List.of(TimeRange.of(sleepTime, wakeTime));
    }
}
