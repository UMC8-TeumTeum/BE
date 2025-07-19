package umc.teumteum.server.global.monitoring.service;

import java.util.Map;

public interface InfraRedisService {
  Map<String, String> checkRedisIsolation();
  String pingNotifiactionRedisTemplate();
  String pingRtRedisTemplate();
  String pingAiContentsRedisTemplate();
}