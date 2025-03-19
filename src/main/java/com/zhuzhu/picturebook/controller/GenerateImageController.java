package com.zhuzhu.picturebook.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.zhuzhu.picturebook.config.AiConfig;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.GenerateResultDTO;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("img")
public class GenerateImageController {
    @Autowired
    private OllamaDeepSeekTextGenerate textGenerate;
    @Autowired
    private RemoteImageGenerate remoteImageGenerate;
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private AppConfig appConfig;
    @GetMapping("generate")
    public String generate() throws Exception {
        String prompt = "生成一个关于中国现代美女的图片提示词，要求皮肤白皙，形象可爱，只需要提示词，不要增加额外的信息";
        String generate = textGenerate.generate("", prompt);
        String s = genImage(generate);
        return AppConfig.videoUrl()+"/img/"+new File(s).getName();
    }
    private String genImage(String prompt) throws Exception {
        return remoteImageGenerate.generate(prompt,AppConfig.videoDir() + File.separator+"img");
    }
}
