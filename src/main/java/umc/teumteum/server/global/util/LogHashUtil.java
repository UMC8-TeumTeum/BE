package umc.teumteum.server.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class LogHashUtil {

    private static final String ALGORITHM = "HmacSHA256";

    @Value("${log.hash.secret}")
    private String secret;

    // prefix 보존 후 식별자 값만 해시
    public String hashIdentifier(String identifier) {
        try {
            int idx = identifier.indexOf('_');

            if (idx != -1) {
                String prefix = identifier.substring(0, idx + 1);
                String value = identifier.substring(idx + 1);
                return prefix + hash(value);
            }

            return hash(identifier);

        } catch (Exception e) {
            return "HASH_FAILED";
        }
    }

    private String hash(String value) {
        try {
            // 1. HmacSHA256 키 초기화
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            mac.init(keySpec);

            // 2. 해시 후 Base64 인코딩 반환
            byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);

        } catch (Exception e) {
            return "HASH_FAILED";
        }
    }
}