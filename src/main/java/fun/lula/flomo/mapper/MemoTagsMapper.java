package fun.lula.flomo.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.lula.flomo.model.entity.MemoTags;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@TableName("memo_tags")
public interface MemoTagsMapper extends BaseMapper<MemoTags> {

}
