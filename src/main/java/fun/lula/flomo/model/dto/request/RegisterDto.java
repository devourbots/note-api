package fun.lula.flomo.model.dto.request;

import lombok.Data;

@Data
public class RegisterDto {
    private String phone;
    private String password;
    private String verifyCode;
}
