package fun.lula.flomo.model.dto.request;

import fun.lula.flomo.model.entity.MemoFiles;
import lombok.Data;


@Data
public class EditMemoDto {
    private String memoId;
    private String content;
    private String userId;
    private String device;

    private MemoFiles[] fileList;

}
