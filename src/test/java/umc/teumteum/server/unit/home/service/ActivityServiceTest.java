package umc.teumteum.server.unit.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.global.validator.ConflictValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.home.ai.generator.AiWishGenerator;
import umc.teumteum.server.domain.home.ai.util.AiWishContentSerializer;
import umc.teumteum.server.domain.home.converter.WishConverter;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.WishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishResponse;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.repository.CategoryRepository;
import umc.teumteum.server.domain.home.repository.WishRepository;
import umc.teumteum.server.domain.home.service.ActivityServiceImpl;
import umc.teumteum.server.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityServiceImpl - Activity(채움활동) 관련 단위 테스트")
class ActivityServiceTest {

  @Mock
  private WishRepository wishRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private WishConverter wishConverter;
  @Mock private AiWishGenerator aiWishGenerator;
  @Mock private AiWishContentSerializer aiWishContentSerializer;


  @InjectMocks
  private ActivityServiceImpl activityService;

  @Mock private User mockUser;

  private List<Wish> dummyWishes;

  @BeforeEach
  void setup() {
    dummyWishes = new ArrayList<>();
    for (long i = 1; i <= 5; i++) {
      Wish wish = Wish.builder()
          .id(i)
          .content("Wish " + i)
          .estimatedDuration(EstimatedDuration.MINUTES_10)
          .build();
      dummyWishes.add(wish);
    }
  }

  @Test
  @DisplayName("[ActivityServiceImpl] - TC1 시간과 카테고리가 모두 일치하는 경우 priority1에서 최대 3개 반환")
  void getMyWish_priority1_max3() {
    // given
    WishOptionRequest request = WishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .categoryId(1L)
        .build();

    Category category = Category.builder()
        .id(1L)
        .name("운동")
        .build();

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(wishRepository.findByUserAndDurationAndWishCategoriesLike(mockUser, EstimatedDuration.MINUTES_10, "운동"))
        .thenReturn(dummyWishes); // 5개

    when(wishConverter.toActivityWishDtoList(any())).thenReturn(Collections.emptyList());

    // when
    WishResponse response = activityService.getMyWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    verify(wishRepository, times(1)).findByUserAndDurationAndWishCategoriesLike(eq(mockUser), any(), eq("운동"));
  }

  @Test
  @DisplayName("[ActivityServiceImpl] - TC2 우선순위1은 없고, 우선순위2(카테고리, 시간 일치)에서 추천되는 경우")
  void getMyWish_priority2_used() {
    // given
    WishOptionRequest request = WishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .customCategory("휴식")
        .build();

    when(wishRepository.findByUserAndDurationAndWishCategoriesLike(mockUser, EstimatedDuration.MINUTES_10, "휴식"))
        .thenReturn(List.of()); // priority1 비어있음
    when(wishRepository.findByUserAndEstimatedDuration(mockUser, EstimatedDuration.MINUTES_10))
        .thenReturn(List.of(dummyWishes.get(0), dummyWishes.get(1))); // 시간만 일치
    when(wishRepository.findByUserAndCategoryLike(mockUser, "휴식"))
        .thenReturn(List.of(dummyWishes.get(2))); // 카테고리만 일치
    when(wishConverter.toActivityWishDtoList(any())).thenReturn(Collections.emptyList());

    // when
    WishResponse response = activityService.getMyWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    verify(wishRepository).findByUserAndEstimatedDuration(eq(mockUser), any());
    verify(wishRepository).findByUserAndCategoryLike(eq(mockUser), eq("휴식"));
  }

  @Test
  @DisplayName("[ActivityServiceImpl] - TC3 customCategory와 categoryId가 동시에 존재하면 예외 발생")
  void getMyWish_bothCategoryFields_throwsException() {
    // given
    WishOptionRequest request = WishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .categoryId(1L)
        .customCategory("운동")
        .build();

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.getMyWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._CATEGORY_INPUT_CONFLICT.getCode());
    assertThat(ex.getErrorReason().getMessage()).isEqualTo(HomeErrorStatus._CATEGORY_INPUT_CONFLICT.getMessage());
  }


  @Test
  @DisplayName("[ActivityServiceImpl] - TC4 categoryId로 조회했지만 존재하지 않으면 예외 발생")
  void getMyWish_categoryId_notFound() {
    // given
    WishOptionRequest request = WishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .categoryId(99L)
        .build();

    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.getMyWish(mockUser, request));


    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._CATEGORY_NOT_FOUND.getCode());
    assertThat(ex.getErrorReason().getMessage()).isEqualTo(HomeErrorStatus._CATEGORY_NOT_FOUND.getMessage());

  }

  @Test
  @DisplayName("[ActivityServiceImpl] - TC5 category, duration 둘 다 비어있을 경우 예외 발생")
  void getMyWish_noCategory_throwsException() {
    // given
    WishOptionRequest request = WishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .build(); // categoryId도 없고 custom도 없음

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.getMyWish(mockUser, request));


    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._CATEGORY_REQUIRED.getCode());
    assertThat(ex.getErrorReason().getMessage()).isEqualTo(HomeErrorStatus._CATEGORY_REQUIRED.getMessage());


  }
}


  // ========== New Tests for AI Wish Generation Feature (userJob parameter) ==========

  @Mock
  private RedisTemplate<String, String> aiContentsRedisTemplate;
  @Mock
  private ValueOperations<String, String> valueOperations;
  @Mock
  private ScheduleRepository scheduleRepository;
  @Mock
  private ConflictValidator conflictValidator;

  @Test
  @DisplayName("[getAiWish] - TC6 AI 위시 생성 성공 - 사용자의 직업 정보를 포함하여 생성")
  void getAiWish_withUserJob_success() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    Category category = Category.builder()
        .id(1L)
        .name("운동")
        .build();

    when(mockUser.getId()).thenReturn(1L);
    when(mockUser.getJob()).thenReturn("개발자");
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(aiContentsRedisTemplate.hasKey(any())).thenReturn(false);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);

    List<ActivityResponseDto.AiWishDto> aiWishes = List.of(
        ActivityResponseDto.AiWishDto.builder()
            .id("uuid-1")
            .title("코드 리뷰하기")
            .content("동료의 PR을 꼼꼼히 리뷰하며 코드 품질 향상에 기여하는 활동")
            .estimatedDuration(EstimatedDuration.MINUTES_10)
            .build()
    );

    when(aiWishGenerator.generate(any(), eq("운동"), eq("집"), eq("개발자"))).thenReturn(aiWishes);
    when(aiWishContentSerializer.serialize(aiWishes)).thenReturn("serialized-json");

    // when
    ActivityResponseDto.AiWishResponse response = activityService.getAiWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getAiContents()).hasSize(1);
    verify(aiWishGenerator).generate(any(), eq("운동"), eq("집"), eq("개발자"));
    verify(valueOperations).set(any(), eq("serialized-json"), any(Duration.class));
  }

  @Test
  @DisplayName("[getAiWish] - TC7 기존 Redis 캐시가 있을 경우 삭제 후 새로 생성")
  void getAiWish_existingCache_deletesAndCreatesNew() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_20)
        .customLocation("카페")
        .customCategory("독서")
        .build();

    when(mockUser.getId()).thenReturn(2L);
    when(mockUser.getJob()).thenReturn("디자이너");
    when(aiContentsRedisTemplate.hasKey(any())).thenReturn(true);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);

    List<ActivityResponseDto.AiWishDto> oldWishes = List.of(
        ActivityResponseDto.AiWishDto.builder()
            .id("old-uuid-1")
            .title("Old Title")
            .content("Old Content")
            .estimatedDuration(EstimatedDuration.MINUTES_20)
            .build()
    );

    List<ActivityResponseDto.AiWishDto> newWishes = List.of(
        ActivityResponseDto.AiWishDto.builder()
            .id("new-uuid-1")
            .title("UI 스케치하기")
            .content("카페에서 여유롭게 새로운 UI 아이디어를 스케치북에 그리는 활동")
            .estimatedDuration(EstimatedDuration.MINUTES_20)
            .build()
    );

    when(valueOperations.get(any())).thenReturn("old-serialized");
    when(aiWishContentSerializer.deserialize("old-serialized")).thenReturn(oldWishes);
    when(aiWishGenerator.generate(any(), eq("독서"), eq("카페"), eq("디자이너"))).thenReturn(newWishes);
    when(aiWishContentSerializer.serialize(newWishes)).thenReturn("new-serialized");

    // when
    ActivityResponseDto.AiWishResponse response = activityService.getAiWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    verify(aiContentsRedisTemplate, times(3)).delete(any(String.class));
    verify(aiWishGenerator).generate(any(), eq("독서"), eq("카페"), eq("디자이너"));
  }

  @Test
  @DisplayName("[getAiWish] - TC8 customLocation과 customCategory 사용 시 정상 동작")
  void getAiWish_withCustomFields_success() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_30)
        .customLocation("공원")
        .customCategory("명상")
        .build();

    when(mockUser.getId()).thenReturn(3L);
    when(mockUser.getJob()).thenReturn("요가 강사");
    when(aiContentsRedisTemplate.hasKey(any())).thenReturn(false);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);

    List<ActivityResponseDto.AiWishDto> aiWishes = List.of(
        ActivityResponseDto.AiWishDto.builder()
            .id("uuid-meditation")
            .title("호흡 명상하기")
            .content("공원의 신선한 공기를 마시며 깊은 호흡과 함께 마음을 정리하는 명상 시간")
            .estimatedDuration(EstimatedDuration.MINUTES_30)
            .build()
    );

    when(aiWishGenerator.generate(any(), eq("명상"), eq("공원"), eq("요가 강사"))).thenReturn(aiWishes);
    when(aiWishContentSerializer.serialize(aiWishes)).thenReturn("serialized");

    // when
    ActivityResponseDto.AiWishResponse response = activityService.getAiWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getAiContents()).hasSize(1);
    verify(aiWishGenerator).generate(any(), eq("명상"), eq("공원"), eq("요가 강사"));
  }

  @Test
  @DisplayName("[getAiWish] - TC9 locationId와 customLocation 동시 입력 시 예외 발생")
  void getAiWish_bothLocationFields_throwsException() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .customLocation("회사")
        .categoryId(1L)
        .build();

    when(mockUser.getId()).thenReturn(1L);

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.getAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._LOCATION_INPUT_CONFLICT.getCode());
  }

  @Test
  @DisplayName("[getAiWish] - TC10 location 정보가 없을 경우 예외 발생")
  void getAiWish_noLocation_throwsException() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .categoryId(1L)
        .build();

    when(mockUser.getId()).thenReturn(1L);

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.getAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._LOCATION_REQUIRED.getCode());
  }

  @Test
  @DisplayName("[getAiWish] - TC11 잘못된 locationId 입력 시 예외 발생")
  void getAiWish_invalidLocationId_throwsException() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(999L)
        .categoryId(1L)
        .build();

    when(mockUser.getId()).thenReturn(1L);

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.getAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._LOCATION_NOT_FOUND.getCode());
  }

  @Test
  @DisplayName("[assignAiWish] - TC12 AI 위시를 스케줄로 저장 성공 - content 포함")
  void assignAiWish_withContent_success() {
    // given
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("test-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    when(mockUser.getId()).thenReturn(1L);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("AI_WISH:WISH_TITLE:test-uuid")).thenReturn("산책하기");
    when(valueOperations.get("AI_WISH:WISH_PARENT:test-uuid")).thenReturn("parent-key");

    List<ActivityResponseDto.AiWishDto> aiWishes = List.of(
        ActivityResponseDto.AiWishDto.builder()
            .id("test-uuid")
            .title("산책하기")
            .content("공원을 따라 여유롭게 걸으며 자연을 느끼는 활동입니다.")
            .estimatedDuration(EstimatedDuration.MINUTES_30)
            .build()
    );

    when(valueOperations.get("parent-key")).thenReturn("serialized-parent");
    when(aiWishContentSerializer.deserialize("serialized-parent")).thenReturn(aiWishes);
    when(scheduleRepository.existsConflictSchedule(any(), any(), any(), any())).thenReturn(false);

    Schedule mockSchedule = Schedule.builder().id(100L).build();
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(mockSchedule);
    when(wishConverter.toScheduleFromAiWish(eq(mockUser), eq(request), eq("산책하기"), 
        eq("공원을 따라 여유롭게 걸으며 자연을 느끼는 활동입니다."))).thenReturn(mockSchedule);

    // when
    ActivityResponseDto.AiSaveResponse response = activityService.assginAiWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(100L);
    verify(conflictValidator).validateTodo(eq(mockUser), eq(startTime), eq(endTime));
    verify(scheduleRepository).save(any(Schedule.class));
  }

  @Test
  @DisplayName("[assignAiWish] - TC13 종료시간이 시작시간보다 이전이면 예외 발생")
  void assignAiWish_endTimeBeforeStartTime_throwsException() {
    // given
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 9, 0);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("test-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.assginAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._INVALID_TIME_RANGE.getCode());
  }

  @Test
  @DisplayName("[assignAiWish] - TC14 시작시간과 종료시간이 같으면 예외 발생")
  void assignAiWish_sameStartAndEndTime_throwsException() {
    // given
    LocalDateTime time = LocalDateTime.of(2024, 1, 15, 10, 0);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("test-uuid")
        .startTime(time)
        .endTime(time)
        .build();

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.assginAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._INVALID_TIME_RANGE.getCode());
  }

  @Test
  @DisplayName("[assignAiWish] - TC15 스케줄 충돌 시 예외 발생")
  void assignAiWish_scheduleConflict_throwsException() {
    // given
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 11, 0);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("test-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    when(mockUser.getId()).thenReturn(1L);
    when(scheduleRepository.existsConflictSchedule(any(), any(), any(), any())).thenReturn(true);

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.assginAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._SCHEDULE_CONFLICT.getCode());
  }

  @Test
  @DisplayName("[assignAiWish] - TC16 Redis에 AI 위시가 없으면 예외 발생")
  void assignAiWish_wishNotFoundInRedis_throwsException() {
    // given
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 11, 0);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("non-existent-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    when(mockUser.getId()).thenReturn(1L);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("AI_WISH:WISH_TITLE:non-existent-uuid")).thenReturn(null);
    when(scheduleRepository.existsConflictSchedule(any(), any(), any(), any())).thenReturn(false);

    // when
    HomeException ex = (HomeException) catchThrowable(() -> activityService.assginAiWish(mockUser, request));

    // then
    assertThat(ex).isInstanceOf(HomeException.class);
    assertThat(ex.getErrorReason().getCode()).isEqualTo(HomeErrorStatus._AI_WISH_NOT_FOUND.getCode());
  }

  @Test
  @DisplayName("[assignAiWish] - TC17 저장 후 Redis 캐시 정리 확인")
  void assignAiWish_cleansUpRedisCache_success() {
    // given
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("test-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    when(mockUser.getId()).thenReturn(1L);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("AI_WISH:WISH_TITLE:test-uuid")).thenReturn("Test Title");
    when(valueOperations.get("AI_WISH:WISH_PARENT:test-uuid")).thenReturn("parent-key");

    List<ActivityResponseDto.AiWishDto> aiWishes = List.of(
        ActivityResponseDto.AiWishDto.builder()
            .id("test-uuid")
            .title("Test Title")
            .content("Test Content")
            .estimatedDuration(EstimatedDuration.MINUTES_30)
            .build(),
        ActivityResponseDto.AiWishDto.builder()
            .id("other-uuid")
            .title("Other Title")
            .content("Other Content")
            .estimatedDuration(EstimatedDuration.MINUTES_30)
            .build()
    );

    when(valueOperations.get("parent-key")).thenReturn("serialized");
    when(aiWishContentSerializer.deserialize("serialized")).thenReturn(aiWishes);
    when(scheduleRepository.existsConflictSchedule(any(), any(), any(), any())).thenReturn(false);

    Schedule mockSchedule = Schedule.builder().id(100L).build();
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(mockSchedule);

    // when
    activityService.assginAiWish(mockUser, request);

    // then
    verify(aiContentsRedisTemplate, times(4)).delete(any(String.class)); // 2 wishes * 2 keys each
  }

  @Test
  @DisplayName("[assignAiWish] - TC18 content가 null인 경우도 정상 처리")
  void assignAiWish_withNullContent_success() {
    // given
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    ActivityRequestDto.AiWishSaveRequest request = ActivityRequestDto.AiWishSaveRequest.builder()
        .id("test-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    when(mockUser.getId()).thenReturn(1L);
    when(aiContentsRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("AI_WISH:WISH_TITLE:test-uuid")).thenReturn("Test Title");
    when(valueOperations.get("AI_WISH:WISH_PARENT:test-uuid")).thenReturn(null);
    when(scheduleRepository.existsConflictSchedule(any(), any(), any(), any())).thenReturn(false);

    Schedule mockSchedule = Schedule.builder().id(100L).build();
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(mockSchedule);
    when(wishConverter.toScheduleFromAiWish(eq(mockUser), eq(request), eq("Test Title"), 
        eq(null))).thenReturn(mockSchedule);

    // when
    ActivityResponseDto.AiSaveResponse response = activityService.assginAiWish(mockUser, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(100L);
    verify(wishConverter).toScheduleFromAiWish(eq(mockUser), eq(request), eq("Test Title"), eq(null));
  }
}