package com.picture.book.generate.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisOutput;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.fastjson.JSONObject;
import com.picture.book.config.AppConfig;
import com.picture.book.generate.IImageGenerate;
import com.picture.book.generate.impl.AbstractImageGenerate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class TongYiImageGenerate extends AbstractImageGenerate implements IImageGenerate {

    public  String generate(String actors ,String scene,String caption) throws Exception {
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(AppConfig.apiKey())
                        .model("wanx2.1-t2i-plus")
//                        .parameter("prompt_extend",false)
                        .prompt(String.format("卡通风格插画，人物描述：%s。场景描述：%s。",actors,scene))
                        .n(1)
                        .size("1280*720")
                        .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            log.info("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
            log.info("image result:"+ JSONObject.toJSONString(result));
        } catch (ApiException | NoApiKeyException e) {
            throw new RuntimeException(e.getMessage());
        }

        ImageSynthesisOutput output = result.getOutput();
        if (output == null) {
            return StrUtil.EMPTY;
        }
        List<Map<String, String>> results = output.getResults();
        if (results == null || results.isEmpty()) {
            return StrUtil.EMPTY;
        }
        String s = results.get(0).get("url");
        String path = AppConfig.tempDir()+File.separator+UUID.randomUUID()+".png";
        HttpUtil.downloadFileFromUrl(s,new File(path));
        String newPath =  addCaption(path,caption);
        FileUtil.del(new File(path).getAbsolutePath());
        return new File(newPath).getAbsolutePath();
    }




}
