package umc.teumteum.server.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.service.NotificationService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.status.SuccessStatus;

@Tag(name="Notifications", description = "사용자 알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationServiceImpl;

  @GetMapping(produces = "application/json")
  @Operation(
      summary = "알림 목록 조회",
      description = "로그인한 사용자의 알림 목록을 조회합니다."
  )
  public ApiResponse<List<NotificationResponseDto>> getNotifications(
      @CurrentUser @Parameter(hidden = true) User user){
    List<NotificationResponseDto> notifications = notificationServiceImpl.getNotifications(user);
    return ApiResponse.of(SuccessStatus._OK, notifications);
  }

}
