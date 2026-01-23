package umc.teumteum.server.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.exception.status.NotificationSuccessStatus;
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
    return ApiResponse.of(NotificationSuccessStatus.NOTIFICATION_LIST_FETCH_SUCCESS, notifications);
  }

  @PatchMapping(value = "/{notificationId}/read", produces = "application/json")
  @Operation(
            summary = "알림 읽음 처리",
            description = "로그인한 사용자의 특정 알림을 읽음(isRead=true) 처리합니다."
  )
  public ApiResponse<NotificationResponseDto.ReadResponseDto> readNotification(
            @CurrentUser @Parameter(hidden = true) User user,
            @PathVariable @Positive(message = "notificationId는 양수여야 합니다.")
            @Parameter(name = "notificationId", description = "읽음 처리할 알림 ID", example = "1")
            Long notificationId
  ) {
      NotificationResponseDto.ReadResponseDto result = notificationServiceImpl.readNotification(user, notificationId);
      return ApiResponse.of(NotificationSuccessStatus.NOTIFICATION_READ_SUCCESS, result);
  }

}
