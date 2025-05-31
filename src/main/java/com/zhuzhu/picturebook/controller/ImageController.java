package com.zhuzhu.picturebook.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.config.MqConfig;
import com.zhuzhu.picturebook.dto.GenImageDTO;
import com.zhuzhu.picturebook.dto.DeleteImageRequestDTO;
import com.zhuzhu.picturebook.dto.ImageGenerateRequestDTO;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.service.MqService;
import com.zhuzhu.picturebook.util.UrlUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("img")
@Slf4j
public class ImageController {
    public static final AtomicLong taskCount = new AtomicLong(0);
    private final ReentrantLock lock = new ReentrantLock();
    @Autowired
    private MqService mqService;
    public static final String workDir;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    static {
        try {
            workDir = AppConfig.videoDir() + File.separator + "img";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private RemoteImageGenerate remoteImageGenerate;


    private void addMq(String prompt, String batchNo) {
        GenImageDTO dto = new GenImageDTO();
        dto.setBatchNo(batchNo);
        dto.setPrompt(prompt);
        dto.setStep(50);
        mqService.addImage(dto);

    }

    @PostMapping("generate")
    public String generate(@RequestBody ImageGenerateRequestDTO requestDTO) throws Exception {
        lock.lock();
        taskCount.getAndAdd(requestDTO.getBatchSize());
        try {
            log.info("put a queue {}", JSONObject.toJSONString(requestDTO));
            Integer batchSize = requestDTO.getBatchSize();
            String prompt = requestDTO.getPrompt();
            if (batchSize == null) {
                batchSize = 1;
            }
            String batchNo = UUID.fastUUID().toString();
            for (int i = 0; i < batchSize; i++) {
                try {
                    addMq(prompt, batchNo);
//                            genImage(prompt);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("genImage end {}", JSONObject.toJSONString(requestDTO));
        } catch (Exception e) {
            log.error("take queue error{}", ExceptionUtil.getMessage(e));
        }
        lock.unlock();


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

    @GetMapping("clearQueue")
    public String clearQueue() throws Exception {

        // RabbitMQ服务器地址和端口
        String host = "192.168.1.4";
        int port = 15672;

        // RabbitMQ用户名和密码
        String username = "admin";
        String password = "123456";

        // 虚拟主机（/ 经过URL编码为 %2f）
        String vhost = "%2f";

        // 队列名称
        String queueName = "PICTURE_GEN_IMG_QUEUE"; // 替换为你实际的队列名称

        // 构建URL
        String url = String.format("http://%s:%d/api/queues/%s/%s/contents",host, port, vhost, queueName);

        // 创建基本认证字符串
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        // 创建HttpClient实例
        String jsonBody = "{\"vhost\":\"/\",\"name\":\"PICTURE_GEN_IMG_QUEUE\",\"mode\":\"purge\"}";
        Method me = Method.DELETE;
       String result =  HttpUtil.createRequest(me,url).header("Authorization","Basic " + encodedAuth).body(jsonBody).execute().body();

      log.info(" result {}",result);


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
    public Integer taskCount() throws Exception {

        return getMessageCount();
    }

    public Integer getMessageCount() {

        String queue = MqConfig.GEN_QUEUE_AME;

        AMQP.Queue.DeclareOk declareOk = rabbitTemplate.execute(new ChannelCallback<AMQP.Queue.DeclareOk>() {
            @NotNull
            public AMQP.Queue.DeclareOk doInRabbit(@NotNull Channel channel) throws Exception {
                return channel.queueDeclarePassive(queue);
            }
        });
        return declareOk.getMessageCount();
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
