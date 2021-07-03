package fun.lula.flomo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

@Data
@TableName(value = "memo_files")
@ToString
public class MemoFiles {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String memoId;
    private String name;
    private String fileKey;
    private Integer fileSize;
    private String url;

}
