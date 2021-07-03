package fun.lula.flomo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@TableName(value = "memo")
@ToString
public class Memo {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String content;
    private String parentId;
    private String device;
    private String createTime;
    private String userId;

    @TableField(exist = false)
    private List<MemoFiles> files;
}
