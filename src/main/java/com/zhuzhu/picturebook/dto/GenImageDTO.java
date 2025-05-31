package com.zhuzhu.picturebook.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class GenImageDTO implements Serializable {
    private String batchNo;
    private String prompt;
    private Integer step = 20;
}
