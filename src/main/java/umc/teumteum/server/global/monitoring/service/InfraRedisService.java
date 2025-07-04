package umc.teumteum.server.global.monitoring.service;

public interface InfraRedisService {
  void setKeyValue(String key, String value);
  String getValue(String key);
  void deleteKey(String key);
  String ping();
}