package com.zhuzhu.picturebook.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenImageDoneDTO implements Serializable {
    private String batchNo;
    private String prompt;
    private String img ;
}
