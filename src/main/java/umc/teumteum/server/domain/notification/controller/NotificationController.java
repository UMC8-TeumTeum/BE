package umc.teumteum.server.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Notifications", description = "사용자 알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  @GetMapping
  @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
  public ResponseEntity<Object> getNotifications(){
    // TODO : 알림 목록 조회 로직 구현
    return null;
  }



}
