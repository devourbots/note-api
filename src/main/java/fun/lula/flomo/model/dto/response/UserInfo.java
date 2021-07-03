package fun.lula.flomo.model.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    private String userId;
    private String nickName;
    private Integer userStatus;
    private String joinDate;
    private Integer joinDays;
    private String tagNums;
    private String memoNums;
}
