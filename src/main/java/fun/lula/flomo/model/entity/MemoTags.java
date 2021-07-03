package fun.lula.flomo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;

@Data
@TableName(value = "memo_tags")
public class MemoTags {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String memoId;
    private String tagsId;

}
