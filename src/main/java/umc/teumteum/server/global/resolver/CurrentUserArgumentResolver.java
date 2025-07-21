package umc.teumteum.server.global.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    // @CurrentUser 어노테이션이 붙었는지 + 파라미터 타입이 Long인지 검사
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().equals(Long.class);
    }

    // 인증된 사용자 정보에서 userId를 꺼내 Long 타입으로 반환
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        // 1. SecurityContext에서 인증 정보 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 없거나 principal이 userdetails.User 타입이 아니면 예외
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User user)) {
            throw new GlobalHandler(ErrorStatus._UNAUTHORIZED);
        }

        return Long.parseLong(user.getUsername());
    }
}
