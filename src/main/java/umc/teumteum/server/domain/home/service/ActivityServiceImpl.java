package umc.teumteum.server.domain.home.service;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.home.ai.generator.AiWishGenerator;
import umc.teumteum.server.domain.home.ai.util.AiWishContentSerializer;
import umc.teumteum.server.domain.home.converter.WishConverter;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.WishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishResponse;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishResponse;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.repository.CategoryRepository;
import umc.teumteum.server.domain.home.repository.WishRepository;
import umc.teumteum.server.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService{

  private final WishRepository wishRepository;
  private final WishConverter wishConverter;
  private final CategoryRepository categoryRepository;
  private final AiWishGenerator aiWishGenerator;
  private final AiWishContentSerializer contentSerializer;

  @Resource(name = "aiContentsRedisTemplate")
  private RedisTemplate<String, String> aiContentsRedisTemplate;

  @Override
  public WishResponse getMyWish(User user, WishOptionRequest request) {
    EstimatedDuration duration = request.getEstimatedDuration();
    String categoryName = extractCategoryName(request.getCategoryId(), request.getCustomCategory());

    // 1. 시간과 카테고리가 모두 일치하는 경우 (우선순위1)
    List<Wish> priority1 = wishRepository.findByUserAndDurationAndWishCategoriesLike(user, duration, categoryName);

    // 2. 시간만 OR 카테고리만 일치하는 경우 (우선순위2)
    Set<Wish> priority2Set = new HashSet<>();

    List<Wish> durationOnlyList = wishRepository.findByUserAndEstimatedDuration(user, duration);
    priority2Set.addAll(durationOnlyList);

    List<Wish> categoryOnlyList = wishRepository.findByUserAndCategoryLike(user, categoryName);
    priority2Set.addAll(categoryOnlyList);

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

  @Override
    public AiWishResponse getAiWish(User user, AiWishOptionRequest request) {
      // 유저 아이디 추출, 카테고리 이름 검증
      Long userId = user.getId();
      String categoryName = extractCategoryName(request.getCategoryId(), request.getCustomCategory());

      // 1. Redis 키 생성, 캐시 확인 및 Redis 캐시 삭제 (기존 추천 초기화)
      // - Redis 키 생성
      String redisKey = generateRedisKey(userId, request, categoryName);

      // - 기존 캐시가 있으면 무조건 삭제
      if (aiContentsRedisTemplate.hasKey(redisKey)) {
        aiContentsRedisTemplate.delete(redisKey);
      }

      // 2. AI 콘텐츠 생성 (새로운 추천 생성)
      List<AiWishDto> generated = aiWishGenerator.generate(request, categoryName);

      // 3. Redis 캐시에 저장 (TTL 1시간)
      aiContentsRedisTemplate.opsForValue().set(
          redisKey,
          contentSerializer.serialize(generated),
          Duration.ofHours(1)
      );;

      // 4. 변환 후 반환
      return ActivityResponseDto.AiWishResponse.builder()
        .wishes(generated)
        .build();

  }

  private String generateRedisKey(Long userId, AiWishOptionRequest request, String categoryName) {
    return String.format("AI_WISH:%d:%s:%s:%s",
        userId,
        request.getEstimatedDuration(),
        request.getLocation(),
        categoryName
    );
  }


  private String extractCategoryName(Long categoryId, String customCategory) {
    boolean hasCustomCategory = customCategory != null && !customCategory.isBlank();
    boolean hasCategoryId = categoryId != null;

    // 1. 카테고리를 두개 입력한 경우 예외
    if (hasCustomCategory && hasCategoryId) {
      throw new HomeException(HomeErrorStatus._CATEGORY_INPUT_CONFLICT);
    }
    // 2. CustomCategory를 입력한 경우 반환
    if (hasCustomCategory) {
      return customCategory;
    }
    // 3. 선택한 카테고리 Id가 존재하면 DB에서 name 조회 후 반환
    if (hasCategoryId) {
      return categoryRepository.findById(categoryId)
          .orElseThrow(() -> new HomeException(HomeErrorStatus._CATEGORY_NOT_FOUND))
          .getName();
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
