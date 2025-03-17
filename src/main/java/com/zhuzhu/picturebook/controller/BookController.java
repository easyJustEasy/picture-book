package com.zhuzhu.picturebook.controller;


import com.zhuzhu.picturebook.dto.*;
import lombok.extern.slf4j.Slf4j;
import com.zhuzhu.picturebook.service.BookGenerateService;
import com.zhuzhu.picturebook.util.ThreadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("book")
@Slf4j
public class BookController {
    @Autowired
    BookGenerateService bookGenerateService;
    // 生成绘本
    @PostMapping("generate")
    public String generate(@RequestBody GenerateRequestDTO requestDTO) throws Exception {
        String uuid = UUID.randomUUID().toString();
        GenerateResultDTO generate = bookGenerateService.generate(requestDTO, uuid);
        return generate.getVideoUrl();
    }
    // 生成绘本
    @PostMapping("batchGenerate")
    public String batchGenerate(@RequestBody BatchGenerateRequestDTO requestDTO) throws Exception {
        String uuid = UUID.randomUUID().toString();
        ThreadUtil.execute(()->{
            bookGenerateService.generateBatch(requestDTO,uuid);
        });
        return uuid;
    }
    @PostMapping("queryByPage")
    public PageResult queryByPage(@RequestBody QueryByPageRequestDTO requestDTO) throws Exception {

        return bookGenerateService.queryByPage(requestDTO);
    }
    @PostMapping("update")
    public String update(@RequestBody GenerateRequestDTO requestDTO) throws Exception {
        String uuid = UUID.randomUUID().toString();
        ThreadUtil.execute(()->{
            bookGenerateService.generate(requestDTO,uuid);
        });
        return uuid;
    }
    @GetMapping("queryTaskCount")
    public Integer queryTaskCount() throws Exception {

        return 1;
    }
}
