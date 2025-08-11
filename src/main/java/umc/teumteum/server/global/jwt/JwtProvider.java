package umc.teumteum.server.global.jwt;

    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.ExpiredJwtException;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.MalformedJwtException;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.UnsupportedJwtException;
    import io.jsonwebtoken.security.Keys;
    import io.jsonwebtoken.security.SecurityException;
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
    public String generateAccessToken(Long userId, String sessionId) {
        return generateToken(userId, sessionId, accessExpirationMs, "ACCESS");
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(Long userId, String sessionId) {
        return generateToken(userId, sessionId, refreshExpirationMs, "REFRESH");
    }

    // 토큰 생성 (타입 구분)
    private String generateToken(Long userId, String sessionId, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("sessionId", sessionId)
                .claim("type", tokenType)   // ACCESS or REFRESH
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact()
                ;
    }

    // 액세스 토큰 유효성 검사
    public void validateAccessToken(String token) {
        validateToken(token, "ACCESS");
    }

    // 리프레시 토큰 유효성 검사
    public void validateRefreshToken(String token) {
        validateToken(token, "REFRESH");
    }

    // 토큰 유효성 검사
    public void validateToken(String token, String expectedType) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                ;

        // 토큰 타입 확인
        String tokenType = claims.get("type", String.class);
        if (!(expectedType.equals(tokenType))) {
            throw new InvalidTokenTypeException("유효하지 않은 토큰 타입입니다.");
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

    // 토큰에서 sessionId 추출
    public String getSessionIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                ;

        return claims.get("sessionId", String.class);
    }
}
