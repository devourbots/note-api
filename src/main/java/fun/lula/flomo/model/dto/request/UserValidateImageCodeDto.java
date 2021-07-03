package fun.lula.flomo.model.dto.request;

import lombok.Data;

@Data
public class UserValidateImageCodeDto {
    private String tokenId;
    private String phone;
    private String verifyCode;
}
