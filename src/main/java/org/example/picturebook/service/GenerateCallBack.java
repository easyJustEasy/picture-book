package org.example.picturebook.service;

import org.example.picturebook.dto.GenerateResultDTO;

public interface GenerateCallBack {
    void process(GenerateResultDTO dto);
}
