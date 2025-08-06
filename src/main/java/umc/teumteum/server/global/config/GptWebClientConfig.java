package umc.teumteum.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GptWebClientConfig {

  @Bean
  public WebClient getWebClient(GptConfig gptConfig) {
    return WebClient.builder()
        .baseUrl(gptConfig.getApiUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + gptConfig.getApiKey())
        .build();

  }

}
