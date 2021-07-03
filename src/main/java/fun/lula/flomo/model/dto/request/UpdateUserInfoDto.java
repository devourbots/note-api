package fun.lula.flomo.model.dto.request;

import lombok.Data;

@Data
public class UpdateUserInfoDto {
    private String id;
    private String nickName;
    private String oldPassword;
    private String newPassword;
}
