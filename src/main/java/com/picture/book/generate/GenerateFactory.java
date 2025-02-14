package com.picture.book.generate;

import cn.hutool.core.util.StrUtil;
import com.picture.book.consts.BookConsts;
import com.picture.book.dto.GenerateRequestDTO;
import com.picture.book.dto.Story;
import com.picture.book.generate.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class GenerateFactory {
    @Autowired
    private ApplicationContext applicationContext;

    public  ITextGenerate textGenerate(){
        return applicationContext.getBean(TongYiTextGenerate.class);
    }
    public  IImageGenerate imageGenerate(){
        return applicationContext.getBean(TongYiImageGenerate.class);
    }
    public  IVoiceGenerate voiceGenerate(){
        return applicationContext.getBean(TongYiVoiceGenerate.class);
    }
    public  IVideoGenerate videoGenerate(){
        return applicationContext.getBean(FfmpegVideoGenerate.class);
    }
    public Story storyMaker(String system, String prompt) throws Exception {
        Story generate = textGenerate().generate(system, prompt);
        generate.setStorySystemMessage(system);
        generate.setStoryUserMessage(prompt);
        return generate;
    }

    public Story storyMaker(GenerateRequestDTO requestDTO) throws Exception {
        String systemMessage =  StrUtil.replace(BookConsts.system, "%s", requestDTO.getRole());
        String userMessage = requestDTO.getStoryDesc()+BookConsts.tail;
        return storyMaker(systemMessage,userMessage);
    }
}
