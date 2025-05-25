package com.zhuzhu.picturebook.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.dto.DeleteImageRequestDTO;
import com.zhuzhu.picturebook.dto.ImageGenerateRequestDTO;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.util.UrlUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("img")
@Slf4j
public class ImageController {
    private static final String workDir;
    private final BlockingQueue<ImageGenerateRequestDTO> blockingQueue = new LinkedBlockingQueue<>();
    private final AtomicLong taskCount = new AtomicLong(0);
    private final ReentrantLock lock = new ReentrantLock();

    static {
        try {
            workDir = AppConfig.videoDir() + File.separator + "img";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private RemoteImageGenerate remoteImageGenerate;

    @PostConstruct
    public void init() {
        ThreadUtil.execute(() -> {
            while (true) {
                try {
                    ImageGenerateRequestDTO requestDTO = blockingQueue.take();
                    log.info("take a queue {}", JSONObject.toJSONString(requestDTO));
                    Integer batchSize = requestDTO.getBatchSize();
                    String prompt = requestDTO.getPrompt();
                    if (batchSize == null) {
                        batchSize = 1;
                    }
                    for (int i = 0; i < batchSize; i++) {
                        try {
                            genImage(prompt);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            taskCount.decrementAndGet();
                        }
                    }
                    log.info("genImage end {}", JSONObject.toJSONString(requestDTO));
                } catch (InterruptedException e) {
                    log.error("take queue error{}", ExceptionUtil.getMessage(e));
                }
                ThreadUtil.safeSleep(300);
            }

        });
    }

    @PostMapping("generate")
    public String generate(@RequestBody ImageGenerateRequestDTO requestDTO) throws Exception {
        blockingQueue.put(requestDTO);
        taskCount.getAndAdd(requestDTO.getBatchSize());
        log.info("put a queue {}", JSONObject.toJSONString(requestDTO));

        return "success";
    }

    @GetMapping("clear")
    public String clear() throws Exception {
        File file = new File(workDir);
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            try {
                FileUtil.del(listFile.getAbsoluteFile());
            } catch (Exception e) {
            }
        }
        return "success";
    }

    @PostMapping("deleteImg")
    public String deleteImg(@RequestBody DeleteImageRequestDTO image) throws Exception {
        File file = new File(workDir);
        String fileNameFromURL = UrlUtil.getFileNameFromURL(image.getImage());
        File abFile = new File(file.getAbsolutePath() + File.separator + fileNameFromURL);
        try {
            FileUtil.del(abFile.getAbsoluteFile());
        } catch (Exception e) {
        }
        return "success";
    }

    @GetMapping("taskCount")
    public Long taskCount() throws Exception {
        return taskCount.get();
    }

    @GetMapping("list")
    public List<String> list() throws Exception {
        File file = new File(workDir);
        File[] files = file.listFiles();
        assert files != null;
        List<File> fileList = Lists.newArrayList(files);
        // 根据文件创建时间排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                try {
                    Path p1 = f1.toPath();
                    Path p2 = f2.toPath();
                    BasicFileAttributes attr1 = Files.readAttributes(p1, BasicFileAttributes.class);
                    BasicFileAttributes attr2 = Files.readAttributes(p2, BasicFileAttributes.class);
                    return attr2.creationTime().compareTo(attr1.creationTime());
                } catch (IOException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        List<String> list = Lists.newArrayList();
        for (File listFile : Objects.requireNonNull(fileList)) {
            if (listFile.length() == 0) {
                continue;
            }
            list.add(AppConfig.videoUrl() + "/img/" + listFile.getName());
        }
        return list;
    }

    private String genImage(String prompt) throws Exception {
        lock.lock();
        try {
            return remoteImageGenerate.generate(prompt, ImageController.workDir);
        } finally {
            lock.unlock();
        }

    }
}
