package fun.lula.flomo.model.dto.request;

import fun.lula.flomo.model.entity.MemoFiles;
import lombok.Data;

@Data
public class CreateMemoDto {
    private String content;

    private String parentId;

    private String device;

    private MemoFiles[] fileList;

    private String userId;
}
