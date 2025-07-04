package umc.teumteum.server.global.monitoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.monitoring.service.InfraRedisService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "test", description = "Redis 등 인프라 점검을 위한 테스트 API 입니다.")
public class InfraTestController {
  private final InfraRedisService infraRedisService;

  @GetMapping("/redis/set")
  @Operation(summary = "Redis에 Key-Value 저장", description = "Redis에 입력한 Key와 Value를 저장합니다.")
  public ResponseEntity<ApiResponse> setKeyValue(
      @Parameter(description = "저장할 Key", example = "testKey")
      @RequestParam String key,
      @Parameter(description = "저장할 Value", example = "testValue")
      @RequestParam String value) {
    infraRedisService.setKeyValue(key, value);
    return ResponseEntity.ok(ApiResponse.onSuccess("Set = " + key + " = " + value));
  }

  @GetMapping("/redis/get")
  @Operation(summary = "Redis에서 Value 조회")
  public ResponseEntity<ApiResponse> getValue(
      @Parameter(description = "조회할 key", example = "testKey")
      @RequestParam String key
  ){
    String value = infraRedisService.getValue(key);
    return ResponseEntity.ok(ApiResponse.onSuccess("Value = " + value));
  }


  @DeleteMapping("/redis/delete")
  @Operation(summary = "Redis에서 Key 삭제", description = "지정한 Key를 Redis에서 삭제합니다.")
  public ResponseEntity<ApiResponse> deleteKey(
      @Parameter(description = "삭제할 key", example = "testKey")
      @RequestParam String key
  ) {
    infraRedisService.deleteKey(key);
    return ResponseEntity.ok(ApiResponse.onSuccess("Successfully Deleted key: " + key));
  }

  @GetMapping("/redis/ping")
  @Operation(summary = "Redis Ping 테스트", description = "Redis 가 정상적으로 연결되어 있는지 확인합니다.")
  public ResponseEntity<ApiResponse> getPing() {
    String pong = infraRedisService.ping();
    return ResponseEntity.ok(ApiResponse.onSuccess("Redis PING : " + pong));
  }

}
