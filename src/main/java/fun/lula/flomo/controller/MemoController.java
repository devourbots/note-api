package fun.lula.flomo.controller;

import com.qiniu.storage.Api;
import fun.lula.flomo.model.dto.request.CreateMemoDto;
import fun.lula.flomo.model.dto.request.EditMemoDto;
import fun.lula.flomo.model.dto.response.CreateMemoRespDto;
import fun.lula.flomo.model.dto.response.DailyCountRespDto;
import fun.lula.flomo.model.entity.Memo;
import fun.lula.flomo.service.MemoService;
import fun.lula.flomo.util.result.ApiResult;
import org.apache.catalina.core.ApplicationPushBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/memo")
public class MemoController {

    @Resource
    MemoService memoService;

    @PostMapping
    public ApiResult createMemo(@RequestBody CreateMemoDto dto) {
        CreateMemoRespDto createMemoRespDto = memoService.createMemo(dto);
        return ApiResult.SUCCESS(createMemoRespDto);
    }

    @PostMapping("/tags")
    public ApiResult getUserTags() {
        List<String> userTags = memoService.userTags();
        return ApiResult.SUCCESS(userTags);
    }

    @PostMapping("/list")
    public ApiResult getUserMemoList(String queryTag) {
        List<Memo> memoList = memoService.userMemoList(queryTag);
        return ApiResult.SUCCESS(memoList);
    }

    @PostMapping("/del")
    public ApiResult delMemo(String memoId) {
        memoService.delMemo(memoId);
        return ApiResult.SUCCESS();
    }


    @PostMapping("/edit")
    public ApiResult editMemo(@RequestBody EditMemoDto dto) {
        Memo memo = memoService.editMemoContent(dto);
        return ApiResult.SUCCESS(memo);
    }

    @PostMapping("/daily_count")
    public ApiResult getDailyCount() {
        DailyCountRespDto dailyCountRespDto = memoService.dailyMap();
        return ApiResult.SUCCESS(dailyCountRespDto);
    }


    @PostMapping("/file/token")
    public ApiResult getFileUploadToken() {
        String fileUploadToken = memoService.getFileUploadToken();
        return ApiResult.SUCCESS(fileUploadToken);
    }
}
