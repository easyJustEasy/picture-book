package com.picture.book.controller;

import com.picture.book.consts.BookConsts;
import com.picture.book.dto.*;
import com.picture.book.service.BookGenerateService;
import com.picture.book.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
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
        BookConsts.BACK_TASK_COUNT.addAndGet(1);
        ThreadUtil.execute(()->{
            bookGenerateService.generate(requestDTO,uuid);
            BookConsts.BACK_TASK_COUNT.decrementAndGet();
        });
        return uuid;
    }
    // 生成绘本
    @PostMapping("batchGenerate")
    public String batchGenerate(@RequestBody BatchGenerateRequestDTO requestDTO) throws Exception {
        String uuid = UUID.randomUUID().toString();
        BookConsts.BACK_TASK_COUNT.addAndGet(requestDTO.getList().size());
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
        BookConsts.BACK_TASK_COUNT.addAndGet(1);
        ThreadUtil.execute(()->{
            bookGenerateService.generate(requestDTO,uuid);
        });
        return uuid;
    }
    @GetMapping("queryTaskCount")
    public Integer queryTaskCount() throws Exception {

        return BookConsts.BACK_TASK_COUNT.get();
    }
}
