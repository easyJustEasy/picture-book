package org.example.picturebook.service;

import org.example.picturebook.dto.GenerateResultDTO;

public interface IPictureBook {

    String generate(GenerateResultDTO resultDTO, GenerateCallBack callBack) throws Exception;
}
