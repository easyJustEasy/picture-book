package com.zhuzhu.picturebook.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.zhuzhu.picturebook.dao.BookDao;
import com.zhuzhu.picturebook.dto.*;
import lombok.extern.slf4j.Slf4j;
import com.zhuzhu.picturebook.consts.GenerateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BookGenerateService {
    @Autowired
    private IPictureBook pictureBook;
    @Autowired
    BookDao bookDao;

    public GenerateResultDTO generate(GenerateRequestDTO requestDTO, String uuid) {
        GenerateResultDTO dto = new GenerateResultDTO();
        try {
            dto.setRole(requestDTO.getRole());
            dto.setStoryDesc(requestDTO.getStoryDesc());
            dto.setCreateTime(new Date());
            dto.setId(requestDTO.getId());
            dto.setStatus(GenerateStatus.txt_ing.getDesc());
            saveDb(dto, uuid);
            pictureBook.generate(dto, new GenerateCallBack() {
                @Override
                public void process(GenerateResultDTO dto) {
                    saveDb(dto, uuid);
                }
            });
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e));
            dto.setError(e.getMessage());
            dto.setStatus(GenerateStatus.fail.getDesc());
        } finally {
            saveDb(dto, uuid);
        }
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
