package org.example.picturebook.generate.imgage;

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
public class RemoteImageGenerate extends AbstractImageGenerate implements IImageGenerate {
    @Autowired
    private AiConfig aiConfig;

    @Override
    public String generate(String actors, String scene, String caption, String workDir) throws Exception {
        String prompt = makePrompt(actors, scene);
        String filePath = workDir + File.separator + UUID.randomUUID() + ".png";
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
