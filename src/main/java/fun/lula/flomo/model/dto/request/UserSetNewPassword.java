package fun.lula.flomo.model.dto.request;

import lombok.Data;

@Data
public class UserSetNewPassword {
    private String token;
    private String password;
}
