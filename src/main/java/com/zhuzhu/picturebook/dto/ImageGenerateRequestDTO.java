package com.zhuzhu.picturebook.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class ImageGenerateRequestDTO {
    private Integer batchSize;
    private String prompt;
}
