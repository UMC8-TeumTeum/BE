package umc.teumteum.server.domain.home.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishSaveRequest;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.WishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiSaveResponse;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishResponse;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishResponse;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.entity.enums.LocationType;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.repository.CategoryRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.home.repository.WishRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.validator.ConflictValidator;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService{

  private static final String CONTENT_PREFIX = "AI_WISH:";
  private static final String WISH_TITLE_PREFIX  = "AI_WISH:WISH_TITLE:";
  private static final String WISH_PARENT_PREFIX = "AI_WISH:WISH_PARENT:";

  private final WishRepository wishRepository;
  private final WishConverter wishConverter;
  private final CategoryRepository categoryRepository;
  private final ScheduleRepository scheduleRepository;
  private final AiWishGenerator aiWishGenerator;
  private final AiWishContentSerializer contentSerializer;

  private final ConflictValidator conflictValidator;
  private final WishConverter converter;

  @Resource(name = "aiContentsRedisTemplate")
  private RedisTemplate<String, String> aiContentsRedisTemplate;

  @Override
  public WishResponse getMyWish(User user, WishOptionRequest request) {
    EstimatedDuration duration = request.getEstimatedDuration();
    String categoryName = extractCategoryName(request.getCategoryId(), request.getCustomCategory());

    // 1. 시간과 카테고리가 모두 일치하는 경우 (우선순위1)
    List<Wish> priority1 = wishRepository.findByUserAndDurationAndWishCategoriesLike(user, duration,
        categoryName);

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
      String locatoinName = extractLocationName(request.getLocationId(), request.getCustomLocation());
      String categoryName = extractCategoryName(request.getCategoryId(), request.getCustomCategory());

      // 1. Redis 키 생성, 캐시 확인 및 Redis 캐시 삭제 (기존 추천 초기화)
      // - Redis 키 생성
      String parentKey = generateRedisKey(userId, request, categoryName, locatoinName);

      // 1-1) 동일 키가 이미 있으면 부모 + 보조키 전부 삭제
      if (Boolean.TRUE.equals(aiContentsRedisTemplate.hasKey(parentKey))) {
        String prevSerialized = aiContentsRedisTemplate.opsForValue().get(parentKey);
        if (prevSerialized != null) {
          List<AiWishDto> prevList = contentSerializer.deserialize(prevSerialized);
          if (prevList != null) {
            for (AiWishDto prev : prevList) {
              aiContentsRedisTemplate.delete(WISH_TITLE_PREFIX + prev.getId());
              aiContentsRedisTemplate.delete(WISH_PARENT_PREFIX + prev.getId());
            }
          }
        }
        aiContentsRedisTemplate.delete(parentKey);
      }

      // 2. AI 콘텐츠 생성 (새로운 추천 생성)
      List<AiWishDto> generated = aiWishGenerator.generate(request, categoryName, locatoinName);

      // 3. Redis 캐시에 저장 (TTL 1시간) + 보조키 생성
      String serialized = contentSerializer.serialize(generated);
      Duration ttl = Duration.ofHours(1);
      aiContentsRedisTemplate.opsForValue().set(parentKey, serialized, ttl);

      // 3-1) 보조 인덱스 저장: wishId -> title / parentKey
      for (AiWishDto dto : generated) {
        aiContentsRedisTemplate.opsForValue().set(
            WISH_TITLE_PREFIX + dto.getId(), dto.getTitle(), ttl);
        aiContentsRedisTemplate.opsForValue().set(
            WISH_PARENT_PREFIX + dto.getId(), parentKey, ttl);
      }
      // 4. 변환 후 반환
      return ActivityResponseDto.AiWishResponse.builder()
          .aiContents(generated)
          .build();


  }

  @Override
  @Transactional
  public AiSaveResponse assginAiWish(User user, AiWishSaveRequest request) {
    // ai 컨텐츠 투두 등록

    String wishUUID = request.getId();

    // 1. 중복 스케줄 체크
    LocalDate date = request.getStartTime().toLocalDate();
    LocalDateTime startTime = request.getStartTime();
    LocalDateTime endTime = request.getEndTime();

    // 1-1. 틈 & 수면패턴 중복 검사 -> 등록 불가
    conflictValidator.validateTodo(user, startTime, endTime);
    // 1-2. 스케줄 중복 검사 -> 등록 가능
    boolean hasConflict = scheduleRepository.existsConflictSchedule(
        user.getId(), date, startTime, endTime
    );
    if (hasConflict && !request.getIsForce()) {
      // force가 false고 일정이 겹치면 예외
      throw new HomeException(HomeErrorStatus._SCHEDULE_CONFLICT);
    }

    // 2. 키를 가지고 Wish 관련 데이터들 조회 없으면 예외
    String title = aiContentsRedisTemplate.opsForValue().get(WISH_TITLE_PREFIX + wishUUID);
    String parentKey = aiContentsRedisTemplate.opsForValue().get(WISH_PARENT_PREFIX + wishUUID);
    if (title == null) {
      throw new HomeException(HomeErrorStatus._AI_WISH_NOT_FOUND);
    }

    // 3. Dto 변환 후 저장
    Schedule schedule = wishConverter.toScheduleFromAiWish(user, request, title);
    Long scheduleId = scheduleRepository.save(schedule).getId();

    // 4. Redis 정리 (Redis에 저장된 데이터 삭제)
    if (parentKey != null) {
      String parentSerialidex = aiContentsRedisTemplate.opsForValue().get(parentKey);
      if (parentSerialidex != null) {
        List<AiWishDto> dtos = contentSerializer.deserialize(parentSerialidex);
        if (dtos != null && !dtos.isEmpty()) {
          for (AiWishDto dto : dtos) {
            aiContentsRedisTemplate.delete(WISH_TITLE_PREFIX + dto.getId());
            aiContentsRedisTemplate.delete(WISH_PARENT_PREFIX + dto.getId());
          }
        }
      }
      aiContentsRedisTemplate.delete(parentKey);
    }else{
      aiContentsRedisTemplate.delete(WISH_TITLE_PREFIX + wishUUID);
      aiContentsRedisTemplate.delete(WISH_PARENT_PREFIX + wishUUID);
    }

    // 응답값 반환
    return AiSaveResponse.builder()
        .id(scheduleId)
        .build();

  }

  private String generateRedisKey(Long userId, AiWishOptionRequest request, String categoryName, String locatoinName) {
    return String.format(CONTENT_PREFIX + "%s:%s:%s:%s",
        userId,
        request.getEstimatedDuration(),
        categoryName,
        locatoinName
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

  private String extractLocationName(Long locationId, String customLocation) {
    boolean hasCustomLocation = customLocation != null && !customLocation.isBlank();
    boolean hasLocationId = locationId != null;

    // 1. 위치를 두개 입력한 경우 예외
    if (hasCustomLocation && hasLocationId) {
      throw new HomeException(HomeErrorStatus._LOCATION_INPUT_CONFLICT);
    }
    // 2. CustomLocation를 입력한 경우 반환
    if (hasCustomLocation) {
      return customLocation;
    }
    // 3. 선택한 위치 Id가 enum에 존재하면 name 조회 후 반환
      if (hasLocationId) {
        LocationType locationType = Arrays.stream(LocationType.values())
            .filter(type -> type.getLocationId() == locationId)
            .findFirst()
            .orElseThrow(() -> new HomeException(HomeErrorStatus._LOCATION_NOT_FOUND));

        return locationType.getDisplayName();
      }
    // 4. 위치가 아예 없으면 예외
    throw new HomeException(HomeErrorStatus._LOCATION_REQUIRED);
  }

  private List<Wish> randomPick(List<Wish> list, int count) {
    if (list == null || list.isEmpty()) return Collections.emptyList();
    Collections.shuffle(list);
    return list.stream().limit(count).collect(Collectors.toList());
  }
}
