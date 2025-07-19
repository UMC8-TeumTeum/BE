package umc.teumteum.server.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 액세스 토큰 생성
    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessExpirationMs, "ACCESS");
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshExpirationMs, "REFRESH");
    }

    // 토큰 생성 (타입 구분)
    private String generateToken(Long userId, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", tokenType)   // ACCESS or REFRESH
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact()
                ;
    }
}
