package com.zhuzhu.picturebook.covert;

import com.zhuzhu.picturebook.consts.GenerateStatus;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.GenerateResultDTO;

import java.util.Date;

public class PictureBookConvert {
    public static GenerateResultDTO convertBookResultDTO(GenerateRequestDTO requestDTO, String uuid) {
        GenerateResultDTO dto = new GenerateResultDTO();
        dto.setRole(requestDTO.getRole());
        dto.setStoryDesc(requestDTO.getStoryDesc());
        dto.setCreateTime(new Date());
        dto.setId(requestDTO.getId());
        dto.setStatus(GenerateStatus.txt_ing.getDesc());
        dto.setBookType(requestDTO.getBookType());
        return dto;
    }
}
