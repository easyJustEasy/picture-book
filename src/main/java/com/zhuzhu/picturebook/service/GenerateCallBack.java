package com.zhuzhu.picturebook.service;

import com.zhuzhu.picturebook.dto.GenerateResultDTO;

public interface GenerateCallBack {
    void process(GenerateResultDTO dto);
}
