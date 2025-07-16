package umc.teumteum.server.global.monitoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.monitoring.service.InfraRedisService;
import umc.teumteum.server.global.monitoring.service.InfraS3Service;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Redis 등 인프라 점검을 위한 테스트 API 입니다.")
public class InfraTestController {
  private final InfraRedisService infraRedisServiceImpl;
  private final InfraS3Service infraS3ServiceImpl;

  @GetMapping("/redis/check")
  @Operation(summary = "Redis DB 분리 확인", description = "RT, 알림, AI 콘텐츠용 RedisTemplate 각각에 test_key를 저장/조회/삭제하여 DB 분리 여부를 확인합니다.")
  public ResponseEntity<ApiResponse> checkRedisIsolation(){
    Map<String, String> result = infraRedisServiceImpl.checkRedisIsolation();
    return ResponseEntity.ok(ApiResponse.onSuccess(result));
  }

  @GetMapping("/redis/ping/notification")
  @Operation(
      summary = "Notification Redis 연결 확인",
      description = "알림용 RedisTemplate(notificationRedisTemplate)이 정상적으로 Redis(index=1)에 연결되어 있는지 확인합니다."
  )
  public ResponseEntity<ApiResponse> getPingNoticitationRedisTemplate() {
    String pong = infraRedisServiceImpl.pingNotifiactionRedisTemplate();
    return ResponseEntity.ok(ApiResponse.onSuccess("Redis PING :with noticationTemplate " + pong));
  }

  @GetMapping("/redis/ping/ai")
  @Operation(
      summary = "AI 콘텐츠 Redis 연결 확인",
      description = "채움활동의 AI 콘텐츠용 RedisTemplate(aiContentsRedisTemplate)이 Redis(index=2)에 정상적으로 연결되어 있는지 확인합니다."
  )
  public ResponseEntity<ApiResponse> getPingAiRedisTemplate() {
    String pong = infraRedisServiceImpl.pingAiContentsRedisTemplate();
    return ResponseEntity.ok(ApiResponse.onSuccess("Redis PING with aiTemplate: " + pong));
  }


  @GetMapping("/redis/ping/rt")
  @Operation(
      summary = "RT용 Redis 연결 확인",
      description = "실시간 처리용 RedisTemplate(rtRedisTemplate)이 Redis(index=0)에 정상적으로 연결되어 있는지 확인합니다."
  )
  public ResponseEntity<ApiResponse> getPingRtRedisTemplate() {
    String pong = infraRedisServiceImpl.pingRtRedisTemplate();
    return ResponseEntity.ok(ApiResponse.onSuccess("Redis PING with rtTemplate : " + pong));
  }


  @GetMapping("/s3/list")
  @Operation(
      summary = "S3 연결 테스트",
      description = "버킷에 정상적으로 접근되는지 확인하고, 지정한 prefix의 객체 리스트를 조회합니다."
  )
  public ResponseEntity<ApiResponse> listS3Objects() {
    List<String> objects = infraS3ServiceImpl.listObjects();
    return ResponseEntity.ok(ApiResponse.onSuccess(objects));
  }


}
