package umc.teumteum.server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(apiInfo());
  }
  private Info apiInfo() {
    return new Info()
        .title("TeumTeum API 명세서")
        .description("TeumTeum 프로젝트 API 명세 초기 구성")
        .version("1.0.0");
  }
}
