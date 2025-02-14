package com.picture.book.generate.impl;


import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.picture.book.config.AppConfig;
import com.picture.book.generate.IVoiceGenerate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
@Component
public class TongYiVoiceGenerate implements IVoiceGenerate {
    private static final String model = "cosyvoice-v1";
    private static final String voice = "longtong";
    private static int count = 0;
    public  String generate(String text) throws Exception {
        SpeechSynthesisParam param =
                SpeechSynthesisParam.builder()
                        // 若没有将API Key配置到环境变量中，需将下面这行代码注释放开，并将apiKey替换为自己的API Key
                         .apiKey(AppConfig.apiKey())
                        .model(model)
                        .voice(voice)
                        .build();
        SpeechSynthesizer synthesizer =new SpeechSynthesizer(param,null);
        ByteBuffer audio = synthesizer.call(text);
        if (audio==null&&count++<3) {
            return generate(text);
        }
        if(audio==null){
            throw  new RuntimeException("生成语音失败");
        }
        String path = AppConfig.tempDir()+File.separator+ UUID.randomUUID()+".mp3";
        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        count  = 0;
        return file.getAbsolutePath();
    }
}
