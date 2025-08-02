package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto;
import umc.teumteum.server.domain.home.exception.status.HomeSuccessStatus;
import umc.teumteum.server.domain.home.service.ActivityService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.status.SuccessStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
@Tag(name="Activity", description = "채움 활동 관련 API")
public class ActivityController {

  private final ActivityService activityServiceImpl;

  @Operation(
      summary = "채움활동 위시리스트 조회",
      description = "채움활동에서 사용자가 입력한 조건들을 만족하는 '내가 등록한 위시'들을 조회합니다."
  )
  @PostMapping(value = "/user-wishes", produces = "application/json")
  public ApiResponse<ActivityResponseDto.WishResponse> getMyWish(
      @Valid @RequestBody ActivityRequestDto.OptionRequest request,
      @CurrentUser @Parameter(hidden = true) User user
  ) {
    // TODO: 채움활동에서 사용자의 조건을 만족하는 "내가 등록한 위시" 조회 로직 구현
    ActivityResponseDto.WishResponse response = activityServiceImpl.getMyWish(user, request);
    return ApiResponse.of(HomeSuccessStatus._ACTIVITY_LOADED, response);

  }


  @Operation(
      summary = "채움활동 AI 추천 컨텐츠 조회(자동 새로고침 포함)",
      description = "채움활동에서 AI가 추천한 콘텐츠 목록들을 조회합니다. 새로고침 시에도 해당 api를 사용해 주세요."
  )
  @PostMapping(value = "/ai", produces = "application/json")
  public ApiResponse<Object> getAiWish(
  ) {
    // TODO: Redis에서 기존 컨텐츠 확인 후, 채움활동 "AI 추천 컨텐츠" 생성 및 Redis 저장 후 반환 로직 구현
    // 1. Redis 캐시 삭제 (기존 추천 초기화)
    // 2. AI 추천 로직 실행 -> 콘텐츠 목록 생성
    // 3. 콘텐츠 목록을 응답으로 반환
    return null;

  }


  @Operation(
      summary = "AI 컨텐츠 빈틈 채우기(AI 컨텐츠 투두 등록)",
      description = "사용자가 선택한 AI 추천 컨텐츠를 투두에 등록합니다."
  )
  @PostMapping(value = "/ai/assign", produces = "application/json")
  public ApiResponse<Object> assignAiContent(
  ) {
    // TODO: 넘겨준 AI 추천 컨텐츠를 투두에 등록하는 로직 필요
    return null;

  }

}
