package com.zhuzhu.picturebook.generate.voice;


import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import lombok.extern.slf4j.Slf4j;
import com.zhuzhu.picturebook.config.AppConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
@Slf4j
@Component
public class TongYiVoiceGenerate implements IVoiceGenerate{
    private static String model = "cosyvoice-v1";

    public  String generate(String text, String voice,Float speed,String workDir) throws Exception {
        if (StrUtil.isBlankIfStr(voice)) {
            voice = "longtong";
        }
        SpeechSynthesisParam param =
                SpeechSynthesisParam.builder()
                        // 若没有将API Key配置到环境变量中，需将下面这行代码注释放开，并将apiKey替换为自己的API Key
                        .apiKey(AppConfig.apiKey())
                        .model(model)
                        .voice(voice).speechRate(speed)
                        .build();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
        ByteBuffer audio = synthesizer.call(text);
        String path = workDir + File.separator + UUID.randomUUID() + ".mp3";
        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file.getAbsolutePath();
    }

}
