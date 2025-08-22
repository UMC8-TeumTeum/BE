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

