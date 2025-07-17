package umc.teumteum.server.domain.fcm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.fcm.dto.FcmTokenRequestDto;
import umc.teumteum.server.domain.fcm.service.FcmService;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.status.SuccessStatus;

@Tag(name="FCM", description = "FCM 클라이언트 토큰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {
  private final FcmService fcmServiceImpl;

  @Operation(
      summary = "클라이언트 토큰 등록",
      description = "로그인한 사용자의 FCM 토큰을 서버에 등록합니다."
  )
  @PostMapping(value = "/token", produces = "application/json")
  public ApiResponse<Object> registerFcmToken(@RequestBody FcmTokenRequestDto request){
    // TODO : 인증 도입시, userId 부분 교체 예정
    Long userId = 1L;
    fcmServiceImpl.registerFcmToken(userId, request.getFcmToken());
    return ApiResponse.of(SuccessStatus.FCM_REGISTER_SUCCESS, null);
  }


  @Operation(
      summary = "FCM 토큰 비활성화",
      description = "클라이언트 로그아웃 시 사용자의 FCM 토큰을 비활성화합니다."
  )
  @PatchMapping(value = "/token", produces = "application/json")
  public ApiResponse<Object> deleteFcmToken(@RequestBody FcmTokenRequestDto request){
    // TODO : 인증 도입시, userId 부분 교체 예정
    Long userId = 1L;
    fcmServiceImpl.detachFcmToken(userId, request.getFcmToken());
    return ApiResponse.of(SuccessStatus.FCM_DEACTIVATE_SUCCESS, null);

  }


}
