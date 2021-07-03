package fun.lula.flomo.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.lula.flomo.model.entity.Memo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
@TableName("memo")
public interface MemoMapper extends BaseMapper<Memo> {
    List<Memo> findMemoByTagName(@Param("userId") String userId, @Param("tagName") String tagName);

    List<Map> getDailyCount(@Param("userId") String userId);
}
