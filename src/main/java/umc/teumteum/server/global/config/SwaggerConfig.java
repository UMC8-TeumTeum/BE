package umc.teumteum.server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${swagger.server-url}")
  private String serverUrl;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .servers(List.of(new Server().url(serverUrl)));
  }

  private Info apiInfo() {
    return new Info()
        .title("TeumTeum API 명세서")
        .description("TeumTeum 프로젝트 API 명세 구성")
        .version("1.0.0");
  }

}
