package umc.teumteum.server.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Getter
@Configuration
public class GptConfig {
  @Value("${open.api-url}")
  private String apiUrl;
  @Value("${open.api-key}")
  private String apiKey;
  @Value("${openai.model}")
  private String model;
  @Value("${openai.temperature}")
  private double temperature;

  public HttpHeaders buildAuthHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);
    return headers;
  }
}
