package umc.teumteum.server.global.monitoring.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.GlobalHandler;


@Service
public class InfraRedisServiceImpl implements InfraRedisService {

  private final RedisTemplate<String, String> notifiactionRedisTemplate;
  private final RedisTemplate<String, String> aiContentsRedisTemplate;
  private final RedisTemplate<String, String> rtRedisTemplate;

  public InfraRedisServiceImpl(
      @Qualifier("notificationRedisTemplate") RedisTemplate<String, String> notifiactionRedisTemplate,
      @Qualifier("aiContentsRedisTemplate") RedisTemplate<String, String> aiContentsRedisTemplate,
      @Qualifier("rtRedisTemplate") RedisTemplate<String, String> rtRedisTemplate) {
    this.notifiactionRedisTemplate = notifiactionRedisTemplate;
    this.aiContentsRedisTemplate = aiContentsRedisTemplate;
    this.rtRedisTemplate = rtRedisTemplate;
  }


  @Override
  public Map<String, String> checkRedisIsolation() {
    Map<String, String> result = new LinkedHashMap<>();

    rtRedisTemplate.opsForValue().set("test_key", "RT_DATA");
    aiContentsRedisTemplate.opsForValue().set("test_key", "AI_DATA");
    notifiactionRedisTemplate.opsForValue().set("test_key", "NOTI_DATA");

    String rtValue = rtRedisTemplate.opsForValue().get("test_key");
    String aiValue = aiContentsRedisTemplate.opsForValue().get("test_key");
    String notiValue = notifiactionRedisTemplate.opsForValue().get("test_key");

    result.put("RT_DB (index 0)", rtValue);
    result.put("NOTIFICATION_DB (index 1)", notiValue);
    result.put("AI_CONTENTS_DB (index 2)", aiValue);

    rtRedisTemplate.delete("test_key");
    notifiactionRedisTemplate.delete("test_key");
    aiContentsRedisTemplate.delete("test_key");

    return result;
  }

  @Override
  public String pingNotifiactionRedisTemplate() {
    try {
      return notifiactionRedisTemplate.getConnectionFactory().getConnection().ping();
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }
  @Override
  public String pingRtRedisTemplate() {
    try {
      return rtRedisTemplate.getConnectionFactory().getConnection().ping();
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }
  @Override
  public String pingAiContentsRedisTemplate() {
    try {
      return aiContentsRedisTemplate.getConnectionFactory().getConnection().ping();
    } catch (Exception e) {
      throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }
}

