package umc.teumteum.server.unit.home.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.home.converter.WishConverter;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishSaveRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishConverter - Wish 변환 로직 단위 테스트")
class WishConverterTest {

  @InjectMocks
  private WishConverter wishConverter;

  @Test
  @DisplayName("[toActivityWishDto] - TC1 Wish 엔티티를 ActivityWishDto로 변환 - content 포함")
  void toActivityWishDto_withContent_convertsSuccessfully() {
    // given
    Wish wish = Wish.builder()
        .id(1L)
        .title("산책하기")
        .content("공원에서 여유롭게 걷는 활동")
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .build();

    // when
    ActivityResponseDto.WishDto result = wishConverter.toActivityWishDto(wish);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("산책하기");
    assertThat(result.getContent()).isEqualTo("공원에서 여유롭게 걷는 활동");
    assertThat(result.getEstimatedDuration()).isEqualTo(EstimatedDuration.MINUTES_10);
  }

  @Test
  @DisplayName("[toActivityWishDto] - TC2 content가 null인 Wish도 정상 변환")
  void toActivityWishDto_withNullContent_convertsSuccessfully() {
    // given
    Wish wish = Wish.builder()
        .id(2L)
        .title("독서하기")
        .content(null)
        .estimatedDuration(EstimatedDuration.MINUTES_20)
        .build();

    // when
    ActivityResponseDto.WishDto result = wishConverter.toActivityWishDto(wish);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(2L);
    assertThat(result.getTitle()).isEqualTo("독서하기");
    assertThat(result.getContent()).isNull();
    assertThat(result.getEstimatedDuration()).isEqualTo(EstimatedDuration.MINUTES_20);
  }

  @Test
  @DisplayName("[toActivityWishDto] - TC3 빈 문자열 content도 정상 변환")
  void toActivityWishDto_withEmptyContent_convertsSuccessfully() {
    // given
    Wish wish = Wish.builder()
        .id(3L)
        .title("명상하기")
        .content("")
        .estimatedDuration(EstimatedDuration.MINUTES_30)
        .build();

    // when
    ActivityResponseDto.WishDto result = wishConverter.toActivityWishDto(wish);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC4 AI 위시를 스케줄로 변환 - content 포함")
  void toScheduleFromAiWish_withContent_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("uuid-123")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "청계천 산책하기";
    String content = "맑은 날씨에 청계천을 따라 여유롭게 걷는 활동입니다.";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, content);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getTitle()).isEqualTo(title);
    assertThat(result.getDescription()).isEqualTo(content);
    assertThat(result.getType()).isEqualTo(ScheduleType.AI);
    assertThat(result.getDate()).isEqualTo(startTime.toLocalDate());
    assertThat(result.getStartTime()).isEqualTo(startTime);
    assertThat(result.getEndTime()).isEqualTo(endTime);
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC5 content가 null인 경우도 정상 처리")
  void toScheduleFromAiWish_withNullContent_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 14, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 14, 30);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("uuid-456")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "간단한 운동하기";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(title);
    assertThat(result.getDescription()).isNull();
    assertThat(result.getType()).isEqualTo(ScheduleType.AI);
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC6 긴 content도 정상 처리")
  void toScheduleFromAiWish_withLongContent_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 9, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 0);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("uuid-789")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "등산하기";
    String content = "산을 오르며 자연을 만끽하고, 건강한 신체를 유지하며, 정신적인 휴식을 취하는 활동입니다. "
        + "맑은 공기를 마시고 아름다운 풍경을 감상하세요.";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, content);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getDescription()).isEqualTo(content);
    assertThat(result.getDescription().length()).isGreaterThan(50);
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC7 자정을 넘기는 시간도 정상 처리")
  void toScheduleFromAiWish_acrossMidnight_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 23, 30);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 16, 0, 30);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("uuid-midnight")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "야간 산책";
    String content = "밤늦게 조용한 거리를 걷는 활동";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, content);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getDate()).isEqualTo(startTime.toLocalDate());
    assertThat(result.getStartTime()).isEqualTo(startTime);
    assertThat(result.getEndTime()).isEqualTo(endTime);
    assertThat(result.getEndTime().isAfter(result.getStartTime())).isTrue();
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC8 동일 날짜의 여러 시간대 처리")
  void toScheduleFromAiWish_multipleTimes_sameDay() {
    // given
    User user = mock(User.class);
    
    // 오전 일정
    AiWishSaveRequest morningRequest = AiWishSaveRequest.builder()
        .id("morning-uuid")
        .startTime(LocalDateTime.of(2024, 1, 15, 9, 0))
        .endTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .build();

    // 오후 일정
    AiWishSaveRequest afternoonRequest = AiWishSaveRequest.builder()
        .id("afternoon-uuid")
        .startTime(LocalDateTime.of(2024, 1, 15, 15, 0))
        .endTime(LocalDateTime.of(2024, 1, 15, 16, 0))
        .build();

    // when
    Schedule morningSchedule = wishConverter.toScheduleFromAiWish(
        user, morningRequest, "오전 운동", "아침에 하는 가벼운 운동");
    Schedule afternoonSchedule = wishConverter.toScheduleFromAiWish(
        user, afternoonRequest, "오후 산책", "점심 후 여유로운 산책");

    // then
    assertThat(morningSchedule.getDate()).isEqualTo(afternoonSchedule.getDate());
    assertThat(morningSchedule.getStartTime()).isBefore(afternoonSchedule.getStartTime());
    assertThat(morningSchedule.getType()).isEqualTo(ScheduleType.AI);
    assertThat(afternoonSchedule.getType()).isEqualTo(ScheduleType.AI);
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC9 특수문자가 포함된 title과 content 처리")
  void toScheduleFromAiWish_withSpecialCharacters_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("special-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "카페에서 ☕ 마시기!";
    String content = "좋아하는 카페에서 커피를 마시며 \"여유\"를 즐기는 시간 (30분)";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, content);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(title);
    assertThat(result.getDescription()).isEqualTo(content);
    assertThat(result.getTitle()).contains("☕");
    assertThat(result.getDescription()).contains("\"여유\"");
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC10 빈 문자열 content 처리")
  void toScheduleFromAiWish_withEmptyContent_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("empty-content-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "스트레칭";
    String content = "";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, content);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(title);
    assertThat(result.getDescription()).isEqualTo(content);
    assertThat(result.getDescription()).isEmpty();
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC11 한글, 영어, 숫자가 섞인 content 처리")
  void toScheduleFromAiWish_withMixedLanguages_createsSchedule() {
    // given
    User user = mock(User.class);
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 30);

    AiWishSaveRequest request = AiWishSaveRequest.builder()
        .id("mixed-uuid")
        .startTime(startTime)
        .endTime(endTime)
        .build();

    String title = "Python 코딩하기";
    String content = "LeetCode에서 Algorithm 문제 3개를 풀며 코딩 실력을 향상시키는 시간입니다.";

    // when
    Schedule result = wishConverter.toScheduleFromAiWish(user, request, title, content);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(title);
    assertThat(result.getDescription()).isEqualTo(content);
    assertThat(result.getTitle()).contains("Python");
    assertThat(result.getDescription()).contains("LeetCode");
    assertThat(result.getDescription()).contains("3개");
  }

  @Test
  @DisplayName("[toScheduleFromAiWish] - TC12 다양한 EstimatedDuration의 시간 범위 처리")
  void toScheduleFromAiWish_withVariousDurations_createsSchedule() {
    // given
    User user = mock(User.class);

    // 10분 활동
    AiWishSaveRequest tenMinRequest = AiWishSaveRequest.builder()
        .id("10min-uuid")
        .startTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endTime(LocalDateTime.of(2024, 1, 15, 10, 10))
        .build();

    // 1시간 활동
    AiWishSaveRequest oneHourRequest = AiWishSaveRequest.builder()
        .id("1hour-uuid")
        .startTime(LocalDateTime.of(2024, 1, 15, 14, 0))
        .endTime(LocalDateTime.of(2024, 1, 15, 15, 0))
        .build();

    // when
    Schedule tenMinSchedule = wishConverter.toScheduleFromAiWish(
        user, tenMinRequest, "짧은 산책", "10분 간단 산책");
    Schedule oneHourSchedule = wishConverter.toScheduleFromAiWish(
        user, oneHourRequest, "긴 운동", "1시간 집중 운동");

    // then
    assertThat(tenMinSchedule.getEndTime().minusMinutes(10))
        .isEqualTo(tenMinSchedule.getStartTime());
    assertThat(oneHourSchedule.getEndTime().minusMinutes(60))
        .isEqualTo(oneHourSchedule.getStartTime());
  }
}