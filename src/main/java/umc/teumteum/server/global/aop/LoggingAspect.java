package umc.teumteum.server.global.aop;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Around("execution(* umc.teumteum.server.domain..controller..*(..))")
  public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();
    String methodName = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();

    log.info("[START] - {} with args: {}", methodName, Arrays.toString(args));

    try {
      Object result = joinPoint.proceed();
      log.info("[RETURN] - {} → {}", methodName, result);
      return result;
    } catch (Exception e) {
      log.error("[EXCEPTION] - {}: {}", methodName,
          e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
      throw e;
    } finally {
      long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("[END] - {} | time={}ms", methodName, elapsedTime);
    }
  }
}
