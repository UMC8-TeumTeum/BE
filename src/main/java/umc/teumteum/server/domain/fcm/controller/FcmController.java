package umc.teumteum.server.domain.fcm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.fcm.dto.FcmTokenRequestDto;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name="FCM", description = "FCM 클라이언트 토큰 관련 API")
@RestController
@RequestMapping("/api/fcm")
public class FcmController {

  @Operation(
      summary = "클라이언트 토큰 등록",
      description = "로그인한 사용자의 FCM 토큰을 서버에 등록합니다."
  )
  @PostMapping(value = "/token", produces = "application/json")
  public ResponseEntity<Object> registerFcmToken(@RequestBody FcmTokenRequestDto request){
    // TODO : FCM 토큰 등록 로직 구현
    return null;
  }


  @Operation(
      summary = "로그아웃 시 토큰 삭제",
      description = "로그아웃하는 사용자의 FCM 토큰을 삭제합니다."
  )
  @DeleteMapping(value = "/token", produces = "application/json")
  public ResponseEntity<Object> deleteFcmToken(){
    // TODO : FCM 토큰 삭제 로직 구현
    return null;

  }


}
