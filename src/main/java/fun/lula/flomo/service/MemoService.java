package fun.lula.flomo.service;

import fun.lula.flomo.model.dto.request.CreateMemoDto;
import fun.lula.flomo.model.dto.request.EditMemoDto;
import fun.lula.flomo.model.dto.response.CreateMemoRespDto;
import fun.lula.flomo.model.dto.response.DailyCountRespDto;
import fun.lula.flomo.model.entity.Memo;

import java.util.List;
import java.util.Map;

public interface MemoService {

    CreateMemoRespDto createMemo(CreateMemoDto dto);

    List<String> userTags();

    List<Memo> userMemoList(String tagName);

    void delMemo(String memoId);

    Memo editMemoContent(EditMemoDto dto);

    DailyCountRespDto dailyMap();

    String getFileUploadToken();
}
