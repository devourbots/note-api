package fun.lula.flomo.mapper;


import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.lula.flomo.model.entity.MemoFiles;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@TableName(value = "memo_files")
public interface MemoFilesMapper extends BaseMapper<MemoFiles> {

}
