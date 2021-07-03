package fun.lula.flomo.util;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ValidateCodeUtilTest {
    public static void main(String[] args) {
        ValidateCodeUtil.Validate code = ValidateCodeUtil.getRandomCode();
        if (code != null) {
            System.out.println(code.getBase64Str());
            System.out.println(code.getValue().toLowerCase(Locale.ROOT));
        }
    }
}