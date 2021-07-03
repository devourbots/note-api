package fun.lula.flomo.model.dto.request;

import lombok.Data;

@Data
public class SmsVerifyCodeDto {
    public static final String SCENE_CODE_REGISTER = "1001";
    public static final String SCENE_CODE_FORGET_PASSWORD = "1002";

    private String phone;
    private String sceneCode;
    private String device;
}
