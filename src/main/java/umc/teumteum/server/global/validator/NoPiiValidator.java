package umc.teumteum.server.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import umc.teumteum.server.global.annotation.NoPii;

import java.util.regex.Pattern;

public class NoPiiValidator implements ConstraintValidator<NoPii, String> {

    // 전화번호 (010-xxxx-xxxx, 010xxxxxxxx)
    private static final Pattern PHONE =
            Pattern.compile("(01[016789])-?\\d{3,4}-?\\d{4}");

    // 이메일
    private static final Pattern EMAIL =
            Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}");

    // 주민등록번호
    private static final Pattern RRN =
            Pattern.compile("\\d{6}-?\\d{7}");

    // 카드번호 (16자리)
    private static final Pattern CARD =
            Pattern.compile("\\b\\d{4}-?\\d{4}-?\\d{4}-?\\d{4}\\b");

    // 계좌번호 형태 (숫자 10~14자리)
    private static final Pattern ACCOUNT =
            Pattern.compile("\\b\\d{10,14}\\b");

    // URL / SNS
    private static final Pattern URL =
            Pattern.compile("(https?://|www\\.)");

    private static final Pattern[] BLOCK_PATTERNS = {
            PHONE, EMAIL, RRN, CARD, ACCOUNT, URL
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;

        for (Pattern pattern : BLOCK_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return false;
            }
        }
        return true;
    }
}
