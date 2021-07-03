package fun.lula.flomo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class User {
    public final static int STATUS_NORMAL = 0;

    public final static int STATUS_DISABLE = 1;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String phone;
    private String nickName;
    private String password;
    private String createTime;
    private Integer status;
    private String wxOpenId;
}
