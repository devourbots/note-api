package fun.lula.flomo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tags")
public class Tags {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String tag;
    private String userId;

}
