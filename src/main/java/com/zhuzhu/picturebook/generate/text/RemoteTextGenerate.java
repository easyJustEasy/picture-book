package com.zhuzhu.picturebook.generate.text;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.zhuzhu.picturebook.config.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
public class RemoteTextGenerate implements ITextGenerate{
    @Autowired
    private AiConfig aiConfig;
    @Override
    public String generate(String system, String prompt) throws Exception {
        if (StrUtil.isBlankIfStr(system)) {
            system = "你是一个非常有用的AI助手";
        }
        prompt =  String.join("", Arrays.stream(prompt.split("\\r?\\n")).toArray(String[]::new));
        HttpResponse httpResponse = HttpUtil.createPost(aiConfig.getText().getRemoteUrl() )
                .form(Map.of("prompt", prompt, "system", system))
                .execute();
        return httpResponse.body();
    }
}
