package com.zhuzhu.picturebook.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.zhuzhu.picturebook.config.AiConfig;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.GenerateResultDTO;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("img")
public class GenerateImageController {
    @Autowired
    private TongYiTextGenerate textGenerate;
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private AppConfig appConfig;
    @PostMapping("generate")
    public String generate() throws Exception {
        String prompt = "生成一个生成图片的提示词";
        String generate = textGenerate.generate("", prompt);
        String s = genImage(generate);
        return AppConfig.videoUrl()+"/"+new File(s).getName();
    }
    private String genImage(String prompt) throws Exception {
        String filePath = AppConfig.videoDir() + File.separator+"img"+File.separator + UUID.randomUUID() + ".png";
        FileUtil.touch(filePath);
        HttpResponse httpResponse = HttpUtil.createPost(aiConfig.getImage().getRemoteUrl())
                .form(Map.of("prompt", prompt, "step", 20))
                .header("Accept", "image/png")
                .execute();
        InputStream body = httpResponse.bodyStream();
        try (OutputStream outputStream = new FileOutputStream(filePath)) { // 保存到文件或实时处理
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = body.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            FileUtil.del(filePath);
            throw new RuntimeException(e);
        }
        return filePath;
    }
}
