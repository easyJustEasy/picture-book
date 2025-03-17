package org.example.picturebook.generate.voice;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.picturebook.config.AiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class RemoteVoiceGenerate implements IVoiceGenerate {
    @Autowired
    private AiConfig aiConfig;

    @Override
    public String generate(String text, String voice,Float speed, String filePath) throws Exception {
        if (StrUtil.isBlankIfStr(voice)) {
            voice = "longyue";
        }
        text =  String.join("", Arrays.stream(text.split("\\r?\\n")).toArray(String[]::new));
        FileUtil.touch(filePath);
        HttpResponse httpResponse = HttpUtil.createPost(aiConfig.getVoice().getRemoteUrl() )
                .form(Map.of("tts_text", text, "audio", voice,"speed",speed))
                .header("Accept", "audio/mpeg")
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
