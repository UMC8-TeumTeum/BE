package umc.teumteum.server.global.monitoring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.GlobalHandler;


@Service
@RequiredArgsConstructor
public class InfraRedisServiceImpl implements InfraRedisService {
  private final RedisTemplate<String, String> redisTemplate;


  @Override
  public void setKeyValue(String key, String value) {
    try {
      redisTemplate.opsForValue().set(key, value);
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public String getValue(String key) {
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteKey(String key) {
    try {
      Boolean result = redisTemplate.delete(key);
      if (!Boolean.TRUE.equals(result)) {
        throw new GlobalHandler(ErrorStatus._BAD_REQUEST); // 커스텀 상태코드가 더 좋을 수도 있음
      }
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public String ping() {
    try {
      return redisTemplate.getConnectionFactory().getConnection().ping();
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }
}

