package umc.teumteum.server.global.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenTypeException extends AuthenticationException {
    public InvalidTokenTypeException(String msg) {
        super(msg);
    }
}
