package com.zhuzhu.picturebook.service;

import com.zhuzhu.picturebook.dto.GenerateResultDTO;

public interface IPictureBook {

    String generate(GenerateResultDTO resultDTO, GenerateCallBack callBack) throws Exception;
}
