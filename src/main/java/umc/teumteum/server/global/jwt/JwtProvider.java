package umc.teumteum.server.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import umc.teumteum.server.global.exception.InvalidTokenTypeException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

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

    // 토큰 유효성 검사
    public void validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    ;

            // 토큰 타입 확인
            String tokenType = claims.get("type", String.class);
            if (!("ACCESS".equals(tokenType))) {
                throw new InvalidTokenTypeException("유효하지 않은 토큰 타입입니다.");
            }

        } catch (SecurityException e) {
            throw new BadCredentialsException("유효하지 않은 JWT 서명입니다.", e);
        } catch (MalformedJwtException e) {
            throw new BadCredentialsException("손상된 JWT 토큰입니다.", e);
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            throw new BadCredentialsException("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("JWT 클레임이 비어있습니다.", e);
        }
    }

    // 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                ;

        return claims.getSubject();
    }
}
