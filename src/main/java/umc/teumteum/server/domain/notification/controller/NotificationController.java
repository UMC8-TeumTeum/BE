package umc.teumteum.server.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.service.NotificationService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.status.SuccessStatus;

@Tag(name="Notifications", description = "사용자 알림 관련 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationServiceImpl;

  @GetMapping(produces = "application/json")
  @Operation(
      summary = "알림 목록 조회 (무한 스크롤)",
      description = "로그인한 사용자의 알림 목록을 조회합니다. 무한 스크롤 방식이며, page와 size를 쿼리 파라미터로 전달해주셔야 합니다."
  )
  public ApiResponse<NotificationResponseDto.SliceResponseDto> getNotifications(
      @CurrentUser @Parameter(hidden = true) User user,
      @RequestParam(defaultValue = "1") @Min(value = 1, message = "page는 1 이상이어야 합니다.")
      @Parameter(name="page", description = "현재 페이지 번호(1부터 시작)", example = "1")
      int page,
      @RequestParam(defaultValue = "10") @Min(value = 10, message = "size는 10 이상이어야 합니다.")
      @Parameter(name = "size", description = "한 페이지에 가져올 알림 개수", example = "10")
      int size
  ){
    NotificationResponseDto.SliceResponseDto notifications = notificationServiceImpl.getNotifications(user, page, size);
    return ApiResponse.of(SuccessStatus._OK, notifications);
  }

}
