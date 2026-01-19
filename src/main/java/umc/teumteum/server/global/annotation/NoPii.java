package umc.teumteum.server.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import umc.teumteum.server.global.validator.NoPiiValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoPiiValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPii {
    String message() default "개인정보가 포함된 입력은 사용할 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
