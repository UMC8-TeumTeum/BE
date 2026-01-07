package umc.teumteum.server.global.config;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Configuration
public class KakaoOAuthConfig {

    @Bean
    public JwkProvider kakaoJwkProvider() throws Exception {
        // 카카오 JWKS 엔드포인트
        return new JwkProviderBuilder(URI.create("https://kauth.kakao.com/.well-known/jwks.json").toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .build();
    }
}
