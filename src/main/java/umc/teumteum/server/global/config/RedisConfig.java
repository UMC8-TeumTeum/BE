package umc.teumteum.server.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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

  private LettuceConnectionFactory createConnectionFactory(int dbIndex){
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
    configuration.setDatabase(dbIndex);
    if (!password.isEmpty()) {
      configuration.setPassword(RedisPassword.of(password));
    }
    return new LettuceConnectionFactory(configuration);
  }


  @Bean
  public RedisTemplate<String,String> createRedisTemplate(LettuceConnectionFactory factory) {
    factory.afterPropertiesSet();
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    return redisTemplate;
  }

  // RT용 Redis(index0)
  @Bean
  public RedisTemplate<String,String> rtRedisTemplate() {
    return createRedisTemplate(createConnectionFactory(0));
  }

  // 알림용 Redis(index1)
  @Bean
  public RedisTemplate<String,String> notificationRedisTemplate() {
    return createRedisTemplate(createConnectionFactory(1));
  }

  // 채움활동 AI 컨텐츠용 Redis(index2)
  @Bean
  public RedisTemplate<String,String> aiContentsRedisTemplate() {
    return createRedisTemplate(createConnectionFactory(2));
  }

}
