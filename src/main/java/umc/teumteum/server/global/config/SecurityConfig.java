package umc.teumteum.server.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;
import umc.teumteum.server.global.jwt.JwtAuthenticationEntryPoint;
import umc.teumteum.server.global.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Value("${management.endpoint.prometheus.enabled:false}")
    private boolean prometheusEnabled;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // URL 접근 권한
                .authorizeHttpRequests(auth -> {
                  // 소셜 로그인, 토큰 재발급, 개발용 BE 토큰 발급 API 허용
                  auth.requestMatchers("/api/auth/social-login/**", "/api/auth/reissue",
                          "/api/auth/dev-token").permitAll()
                      .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                      .permitAll()   // Swagger 관련 경로 허용
                      .requestMatchers("/actuator/health", "/actuator/info").permitAll();

                  if (prometheusEnabled) {
                    auth.requestMatchers("/actuator/prometheus").permitAll();
                  }
                  auth.requestMatchers(CorsUtils::isPreFlightRequest).permitAll();
                  auth.anyRequest().authenticated(); // 나머지는 인증 필요
                })
                // 예외 처리
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // JWT 검증 필터
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));   // JWT 사용으로 비활성화

        return http.build();
    }
}
