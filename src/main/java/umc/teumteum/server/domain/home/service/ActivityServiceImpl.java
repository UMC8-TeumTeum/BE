package umc.teumteum.server.domain.home.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.home.converter.WishConverter;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.OptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishResponse;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.repository.CategoryRepository;
import umc.teumteum.server.domain.home.repository.WishCategoryRepository;
import umc.teumteum.server.domain.home.repository.WishRepository;
import umc.teumteum.server.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService{

  private final WishRepository wishRepository;
  private final WishCategoryRepository wishCategoryRepository;
  private final WishConverter wishConverter;
  private final CategoryRepository categoryRepository;

  @Override
  public WishResponse getMyWish(User user, OptionRequest request) {
    EstimatedDuration duration = request.getEstimatedDuration();
    String categoryName = extractCategoryName(request);

    // 1. 시간과 카테고리가 모두 일치하는 경우 (우선순위1)
    List<Wish> priority1 = wishRepository.findByUserAndDurationAndWishCategoriesLike(user, duration, categoryName);
    System.out.println("🔍 priority1:");
    priority1.forEach(w -> System.out.println(" - " + w.getId() + ": " + w.getEstimatedDuration()));

    // 2. 시간만 OR 카테고리만 일치하는 경우 (우선순위2)
    Set<Wish> priority2Set = new HashSet<>();

    List<Wish> durationOnlyList = wishRepository.findByUserAndEstimatedDuration(user, duration);
    priority2Set.addAll(durationOnlyList);
    System.out.println("🔍 durationOnlyList:");
    durationOnlyList.forEach(w -> System.out.println(" - " + w.getId() + ": " + w.getEstimatedDuration()));

    List<Wish> categoryOnlyList = wishRepository.findByUserAndCategoryLike(user, categoryName);
    priority2Set.addAll(categoryOnlyList);
    System.out.println("🔍 categoryOnlyList:");
    categoryOnlyList.forEach(w -> System.out.println(" - " + w.getId() + ": " + w.getEstimatedDuration()));

    // 시간 + 카테고리 둘다 만족한 것 제외 (1번에서 구했으니까)
    priority2Set.removeAll(priority1);

    // 3. 위시 최종 선택
    List<Wish> result = new ArrayList<>();
    // 먼저 우선순위 1인 리스트 중에서 랜덤으로 추출
    result.addAll(randomPick(priority1, Math.min(3, priority1.size())));

    // 우선순위 1인 리스트에 있는 위시가 3개 이하라면, 2순위 리스트에 있는 위시틑 랜덤 추출
    int remaining = 3 - result.size();
    if (remaining > 0) {
      result.addAll(randomPick(new ArrayList<>(priority2Set), remaining));
    }

    return ActivityResponseDto.WishResponse.builder()
        .wishes(wishConverter.toActivityWishDtoList(result))
        .build();

  }
  private String extractCategoryName(OptionRequest request) {
    boolean hasCustomCategory = request.getCustomCategory() != null && !request.getCustomCategory().isBlank();
    boolean hasCategoryId = request.getCategoryId() != null;

    // 1. 카테고리를 두개 입력한 경우 예외
    if (hasCustomCategory && hasCategoryId) {
      throw new HomeException(HomeErrorStatus._CATEGORY_INPUT_CONFLICT);
    }
    // 2. CustomCategory를 입력한 경우 반환
    if (hasCustomCategory) {
      return request.getCustomCategory();
    }
    // 3. 선택한 카테고리 Id가 존재하면 DB에서 name 조회 후 반환
    if (hasCategoryId) {
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new HomeException(HomeErrorStatus._CATEGORY_NOT_FOUND));
      return category.getName();
    }
    // 4. 카테고리가 아예 없으면 예외
    throw new HomeException(HomeErrorStatus._CATEGORY_REQUIRED);
  }

  private List<Wish> randomPick(List<Wish> list, int count) {
    if (list == null || list.isEmpty()) return Collections.emptyList();
    Collections.shuffle(list);
    return list.stream().limit(count).collect(Collectors.toList());
  }
}
