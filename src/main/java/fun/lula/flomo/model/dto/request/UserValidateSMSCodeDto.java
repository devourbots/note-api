package fun.lula.flomo.model.dto.request;

import lombok.Data;

@Data
public class UserValidateSMSCodeDto {
    private String token;
    private String verifyCode;
}
