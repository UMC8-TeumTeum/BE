package umc.teumteum.server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${swagger.server-url}")
  private String serverUrl;

  private static final String JWT_SCHEME_NAME = "JWT";

  @Bean
  public OpenAPI openAPI() {

    // JWT 인증 방식 등록
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(JWT_SCHEME_NAME);
    Components components = new Components()
            .addSecuritySchemes(JWT_SCHEME_NAME, new SecurityScheme()
                    .name(JWT_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));

    return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(new Server().url(serverUrl)))
            .components(components)
            .addSecurityItem(securityRequirement)
            ;
  }

  private Info apiInfo() {
    return new Info()
        .title("TeumTeum API 명세서")
        .description("TeumTeum 프로젝트 API 명세 구성")
        .version("1.0.0");
  }
}
