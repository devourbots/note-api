package fun.lula.flomo.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.lula.flomo.exception.ServiceException;
import fun.lula.flomo.mapper.MemoFilesMapper;
import fun.lula.flomo.mapper.MemoMapper;
import fun.lula.flomo.mapper.MemoTagsMapper;
import fun.lula.flomo.mapper.TagsMapper;
import fun.lula.flomo.model.dto.request.CreateMemoDto;
import fun.lula.flomo.model.dto.request.EditMemoDto;
import fun.lula.flomo.model.dto.response.CreateMemoRespDto;
import fun.lula.flomo.model.dto.response.DailyCountRespDto;
import fun.lula.flomo.model.entity.Memo;
import fun.lula.flomo.model.entity.MemoFiles;
import fun.lula.flomo.model.entity.MemoTags;
import fun.lula.flomo.model.entity.Tags;
import fun.lula.flomo.service.MemoService;
import fun.lula.flomo.util.DateTimeUtil;
import fun.lula.flomo.util.QiniuUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.swing.text.html.HTML;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemoServiceImpl implements MemoService {
    @Resource
    MemoMapper memoMapper;

    @Resource
    TagsMapper tagsMapper;

    @Resource
    MemoTagsMapper memoTagsMapper;

    @Resource
    QiniuUtil qiniuUtil;

    @Resource
    MemoFilesMapper memoFilesMapper;

    @Override
    @Transactional
    public CreateMemoRespDto createMemo(CreateMemoDto dto) {
        String userId = StpUtil.getLoginIdAsString();
        Memo memo = new Memo();
        if (StringUtils.isNoneEmpty(dto.getParentId())) {
            Memo parent = memoMapper.selectById(dto.getParentId());
            if (parent != null) {
                memo.setParentId(dto.getParentId());
            }
        }
        memo.setContent(dto.getContent());
        memo.setCreateTime(DateTimeUtil.getNowString2());
        memo.setDevice(dto.getDevice());
        memo.setUserId(userId);
        // 插入memo
        memoMapper.insert(memo);
        // 处理 memo 和 tags 的关联
        List<String> tagsList = saveMemoTags(memo);

        MemoFiles[] fileList = dto.getFileList();
        for (MemoFiles memoFiles : fileList) {
            memoFiles.setMemoId(memo.getId());
            memoFilesMapper.insert(memoFiles);
            log.info("创建 memo 文件{}", memoFiles);
        }

        return new CreateMemoRespDto(memo.getContent(), memo.getCreateTime(), memo.getId(), memo.getDevice(),
                memo.getParentId(), tagsList);
    }

    @Override
    public List<String> userTags() {
        String userId = StpUtil.getLoginIdAsString();
        List<Tags> tagsList = tagsMapper.selectList(new QueryWrapper<Tags>().eq("user_id", userId));
        return tagsList.stream().map(Tags::getTag).collect(Collectors.toList());
    }

    @Override
    public List<Memo> userMemoList(String tagName) {
        String userId = StpUtil.getLoginIdAsString();
        return memoMapper.findMemoByTagName(userId, StringUtils.isNotEmpty(tagName) ? "#" + tagName : "");
    }

    @Override
    public void delMemo(String memoId) {
        String userId = StpUtil.getLoginIdAsString();
        Memo memo = memoMapper.selectById(memoId);
        if (memo == null || !StringUtils.equals(memo.getUserId(), userId)) {
            throw new ServiceException("笔记不存在");
        }
        memoMapper.deleteById(memoId);
        deleteMemoTags(memo);
        deleteMemoFiles(memo);

    }

    private void deleteMemoFiles(Memo memo) {
        List<MemoFiles> memoFilesList = memoFilesMapper.selectList(new QueryWrapper<MemoFiles>().eq("memo_id",
                memo.getId()));

        if (!memoFilesList.isEmpty()) {
            List<String> fileList = memoFilesList.stream().map(MemoFiles::getFileKey).collect(Collectors.toList());
            qiniuUtil.batchDelFile(fileList.stream().toArray(String[]::new));
            log.info("调用API批量删除图片");
            memoFilesMapper.delete(new QueryWrapper<MemoFiles>().eq("memo_id", memo.getId()));
        }
    }

    @Override
    @Transactional
    public Memo editMemoContent(EditMemoDto dto) {
        String userId = StpUtil.getLoginIdAsString();
        dto.setUserId(userId);
        Memo memo = memoMapper.selectById(dto.getMemoId());
        if (memo == null || !StringUtils.equals(dto.getUserId(), userId)) {
            throw new ServiceException("笔记不存在");
        }
        // 直接删除原有的 tags
        deleteMemoTags(memo);
        memo.setContent(dto.getContent());
        // 更新 content 内容
        memoMapper.updateById(memo);
        // 设置新的 tags
        saveMemoTags(memo);


        // 处理图片更新
        updateMemoFilesByMemo(dto);
        return memo;
    }

    private void updateMemoFilesByMemo(EditMemoDto dto) {
        // 用户提交的文件列表
        List<MemoFiles> inputFileList = Arrays.asList(dto.getFileList());
        List<MemoFiles> dbFilesList = memoFilesMapper.selectList(new QueryWrapper<MemoFiles>().eq("memo_id",
                dto.getMemoId()));
        List<MemoFiles> delFileList = new ArrayList<>();
//        List<MemoFiles> newFileList = new ArrayList<>();
        List<MemoFiles> oldFileList = new ArrayList<>();
        for (MemoFiles memoFiles : inputFileList) {
            // 文件 id 为空，说明是用户新上传的文件
            if (StringUtils.isEmpty(memoFiles.getId())) {
//                newFileList.add(memoFiles);
                // 将新增文件存入数据库
                memoFiles.setMemoId(dto.getMemoId());
                memoFilesMapper.insert(memoFiles);
            } else {
                // 文件 id 不为空，说明是存在于数据库的文件，但是此次用户还是需要它
                oldFileList.add(memoFiles);
            }
        }

        // 嵌套循环，找出数据库中需要删除的文件
        for (MemoFiles dbFile : dbFilesList) {
            boolean needDel = true;
            for (MemoFiles oldFile : oldFileList) {
                // 文件一致，用户需要
                if (StringUtils.equals(oldFile.getId(), dbFile.getId())) {
                    needDel = false;
                }
            }
            if (needDel) {
                delFileList.add(dbFile);
            }
        }

        if (delFileList.size() > 0) {
            memoFilesMapper.deleteBatchIds(delFileList.stream().map(memoFiles -> memoFiles.getId()).collect(Collectors.toList()));
            qiniuUtil.batchDelFile(delFileList.stream().map(memoFiles -> memoFiles.getFileKey()).collect(Collectors.toList()).stream().toArray(String[]::new));
        }
    }

    @Override
    public DailyCountRespDto dailyMap() {
        String userId = StpUtil.getLoginIdAsString();
        List<Map> dailyCount = memoMapper.getDailyCount(userId);
        DailyCountRespDto dailyCountRespDto = new DailyCountRespDto();
        dailyCountRespDto.setDailyCount(dailyCount);
        return dailyCountRespDto;
    }

    @Override
    public String getFileUploadToken() {
        String fileUploadToken = qiniuUtil.getFileUploadToken();
        return fileUploadToken;
    }

    private void deleteMemoTags(Memo memo) {
        List<MemoTags> memoTagsList = memoTagsMapper.selectList(new QueryWrapper<MemoTags>().eq("memo_id",
                memo.getId()));
        for (MemoTags memoTags : memoTagsList) {
            // 查询tags是否绑定了其他的笔记
            List<MemoTags> memoTagsList1 = memoTagsMapper.selectList(new QueryWrapper<MemoTags>().eq("tags_id",
                    memoTags.getTagsId()));
            if (memoTagsList1.size() == 1) {
                // 该 tag 没有绑定其他笔记，可以直接删除
                tagsMapper.deleteById(memoTags.getTagsId());
            }
            // 删除笔记与 tag 的关联
            memoTagsMapper.deleteById(memoTags.getId());
        }

    }

    private List<String> saveMemoTags(Memo memo) {
        String content = memo.getContent();
        String text = Jsoup.parse(content).body().text();
        Pattern pattern = Pattern.compile("(#(\\w|[\\u4E00-\\u9FA5]|\\/|\\s)+)");
        Matcher matcher = pattern.matcher(text);
        List<String> tagList = new ArrayList<>();
        while (matcher.find()) {
            String tag = matcher.group(1);
            tag = tag.replaceAll("\\s+$", "");
            tagList.add(tag);
        }
        for (String tagName : tagList) {
            Tags tag = tagsMapper.selectOne(new QueryWrapper<Tags>().eq("user_id", memo.getUserId()).eq("tag",
                    tagName));
            // 判断该用户是否存在该tag，不存在则创建
            if (tag == null) {
                tag = new Tags();
                tag.setTag(tagName);
                tag.setUserId(memo.getUserId());
                tagsMapper.insert(tag);
            }
            MemoTags memoTags = new MemoTags();
            memoTags.setMemoId(memo.getId());
            memoTags.setTagsId(tag.getId());
            //设置 memo 与 tag 的关联
            memoTagsMapper.insert(memoTags);
        }
        return tagList;
    }


}
