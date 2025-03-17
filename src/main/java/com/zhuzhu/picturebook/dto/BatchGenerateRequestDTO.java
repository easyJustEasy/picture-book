package com.zhuzhu.picturebook.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchGenerateRequestDTO {
    private List<GenerateRequestDTO> list;
}
