package umc.teumteum.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.password}")
  private String password;

  @Value("${spring.data.redis.port}")
  private int port;

  private LettuceConnectionFactory createConnectionFactory(int dbIndex) {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
    config.setDatabase(dbIndex);
    if (!password.isEmpty()) {
      config.setPassword(RedisPassword.of(password));
    }
    LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
    factory.afterPropertiesSet();
    return factory;
  }

  private RedisTemplate<String, String> createRedisTemplate(LettuceConnectionFactory factory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }

  // RT용 Redis(index 0)
  @Bean
  public RedisTemplate<String, String> rtRedisTemplate() {
    return createRedisTemplate(createConnectionFactory(0));
  }

  // 알림용 Redis(index 1)
  @Bean
  public RedisTemplate<String, String> notificationRedisTemplate() {
    return createRedisTemplate(createConnectionFactory(1));
  }

  // AI 컨텐츠용 Redis(index 2)
  @Bean
  public RedisTemplate<String, String> aiContentsRedisTemplate() {
    return createRedisTemplate(createConnectionFactory(2));
  }

}