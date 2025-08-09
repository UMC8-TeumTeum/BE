package umc.teumteum.server.global.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenBlacklistException extends AuthenticationException {
    public TokenBlacklistException(String msg) {
        super(msg);
    }
}
