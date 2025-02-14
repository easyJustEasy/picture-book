package com.picture.book.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.picture.book.config.AppConfig;
import com.picture.book.consts.BookConsts;
import com.picture.book.consts.GenerateStatus;
import com.picture.book.dao.BookDao;
import com.picture.book.dto.*;
import com.picture.book.generate.GenerateFactory;
import com.picture.book.generate.IPictureBookGenerate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BookGenerateService {
    @Autowired
    IPictureBookGenerate pictureBookGenerate;
    @Autowired
    private GenerateFactory factory;
    @Autowired
    BookDao bookDao;

    public GenerateResultDTO generate(GenerateRequestDTO requestDTO, String uuid) {
        GenerateResultDTO dto = new GenerateResultDTO();
        dto.setRole(requestDTO.getRole());
        dto.setStoryDesc(requestDTO.getStoryDesc());
        dto.setCreateTime(new Date());
        dto.setId(requestDTO.getId());
        dto.setStatus(GenerateStatus.ing.getDesc());
        Story story = null;
        try {
            GenerateResultDTO resultDTO = bookDao.queryById(requestDTO.getId());
            if (resultDTO != null && StrUtil.isNotBlank(resultDTO.getStoryOutputMessage())) {
                story = Story.parseStory(resultDTO.getStoryOutputMessage());
                story.setStorySystemMessage(resultDTO.getStorySystemMessage());
                story.setStoryUserMessage(resultDTO.getStoryUserMessage());
            } else {
                story = factory.storyMaker(requestDTO);
            }
            dto.setStorySystemMessage(story.getStorySystemMessage());
            dto.setStoryUserMessage(story.getStoryUserMessage());
            dto.setStoryOutputMessage(story.getStoryOutputMessage());
        } catch (Exception e) {
            dto.setError(e.getMessage());
            dto.setStatus(GenerateStatus.fail.getDesc());
            log.error(ExceptionUtil.stacktraceToString(e));
        }
        saveDb(dto, uuid);
        if (story == null) {
            return dto;
        }
        try {
            PictureResultDTO generate = pictureBookGenerate.generate(StrUtil.EMPTY, story);
            File file = new File(generate.getPictureUrl());
            String s = AppConfig.videoUrl() + file.getName();
            dto.setVideoUrl(s);
            dto.setStatus(GenerateStatus.success.getDesc());
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e));
            dto.setError(e.getMessage());
            dto.setStatus(GenerateStatus.fail.getDesc());
        } finally {
            BookConsts.BACK_TASK_COUNT.decrementAndGet();
        }
        saveDb(dto, uuid);
        return dto;
    }

    private void saveDb(GenerateResultDTO dto, String uuid) {
        if (dto.getId() != null) {
            bookDao.update(dto, uuid);
        } else {
            bookDao.save(dto, uuid);
        }
    }

    public List<GenerateResultDTO> generateBatch(BatchGenerateRequestDTO requestDTO, String uuid) {

        return requestDTO.getList().stream().map(generateRequestDTO -> generate(generateRequestDTO, uuid)).toList();
    }

    public PageResult queryByPage(QueryByPageRequestDTO requestDTO) {
        return bookDao.queryByPage(requestDTO);
    }
}
