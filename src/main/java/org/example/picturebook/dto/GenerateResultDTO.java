package org.example.picturebook.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.Date;

@Data
public class GenerateResultDTO {
    private Integer id;
    private String role;
    private String storyDesc;
    private String storySystemMessage;
    private String storyUserMessage;
    private String storyOutputMessage;
    private Date createTime;
    private String videoUrl;
    private String error = StrUtil.EMPTY;
    private String batchId;
    private String status;
}
