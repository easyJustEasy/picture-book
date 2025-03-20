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
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import com.zhuzhu.picturebook.third.audio.AudioPlayer;
import com.zhuzhu.picturebook.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("img")
public class GenerateImageController {
    @Autowired
    private OllamaDeepSeekTextGenerate textGenerate;
    @Autowired
    private RemoteImageGenerate remoteImageGenerate;
    @Autowired
    private RemoteVoiceGenerate voiceGenerate;
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private AppConfig appConfig;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    @GetMapping("generate")
    public String generate() throws Exception {
        String prompt = "生成一个关于中国现代美女的图片提示词，要求皮肤白皙，形象可爱，只需要提示词，不要增加额外的信息";
        String system = """
                  请根据用户输入生成提示词，字数不得超过50个字，不需要输出额外的信息
                """;
        prompt = """
                请生成一个关于一个中国现代美女的提示词，要求年龄是18-30岁，皮肤白皙，形象可爱。
                """;
        String generate = textGenerate.generate(system, prompt);
        String voicePath = voiceGenerate.generate(generate, "悟空-电视剧", 1.0F, FileUtils.getUuidFileName(AppConfig.videoDir() + File.separator + "voice", ".wav"));
        String s = genImage(generate);
        executorService.submit(()->{
            AudioPlayer.playSound(voicePath);
        });
        return AppConfig.videoUrl()+"/img/"+new File(s).getName();
    }
    private String genImage(String prompt) throws Exception {
        return remoteImageGenerate.generate(prompt,AppConfig.videoDir() + File.separator+"img");
    }
}
