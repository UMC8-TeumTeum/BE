package umc.teumteum.server.global.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "test", description = "Redis 등 인프라 점검을 위한 테스트 API 입니다.")
public class TestController {
  private final RedisTemplate<String, String> redisTemplate;

  @GetMapping("/redis/set")
  @Operation(summary = "Redis에 Key-Value 저장", description = "Redis에 입력한 Key와 Value를 저장합니다.")
  public ResponseEntity<String> setKeyValue(
      @Parameter(description = "저장할 Key", example = "testKey")
      @RequestParam String key,
      @Parameter(description = "저장할 Value", example = "testValue")
      @RequestParam String value) {
    redisTemplate.opsForValue().set(key, value);
    return ResponseEntity.ok("Set = " + key + " = " + value);
  }

  @GetMapping("/redis/get")
  @Operation(summary = "Redis에서 Value 조회")
  public ResponseEntity<String> getValue(
      @Parameter(description = "조회할 key", example = "testKey")
      @RequestParam String key
  ){
    String value = redisTemplate.opsForValue().get(key);
    return ResponseEntity.ok("Value = " + value);
  }

  @DeleteMapping("/redis/delete")
  @Operation(summary = "Redis에서 Key 삭제", description = "지정한 Key를 Redis에서 삭제합니다.")
  public ResponseEntity<String> deleteKey(
      @Parameter(description = "삭제할 key", example = "testKey")
      @RequestParam String key
  ) {
    Boolean result = redisTemplate.delete(key);
    if (Boolean.TRUE.equals(result)) {
      return ResponseEntity.ok("Successfully Deleted key: " + key);
    } else {
      return ResponseEntity.status(404).body("Key not found or already deleted: " + key);
    }
  }

  @GetMapping("/redis/ping")
  @Operation(summary = "Redis Ping 테스트", description = "Redis 가 정상적으로 연결되어 있는지 확인합니다.")
  public ResponseEntity<String> getPing() {
    try {
      String pong = redisTemplate.getConnectionFactory()
          .getConnection().ping();
      return ResponseEntity.ok("Redis PING : " + pong);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Redis PING failed:" + e.getMessage());
    }
  }

}
