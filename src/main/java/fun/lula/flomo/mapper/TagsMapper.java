package fun.lula.flomo.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.lula.flomo.model.entity.Tags;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@TableName("tags")
public interface TagsMapper extends BaseMapper<Tags> {

}
