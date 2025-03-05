package com.picture.book;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.picture.book.consts.BookConsts;
import com.picture.book.dto.GenerateRequestDTO;
import com.picture.book.dto.Story;
import com.picture.book.generate.GenerateFactory;
import com.picture.book.generate.impl.OllamaDeepSeekTextGenerate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TestOllama {
    @Autowired
    OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;
    @Autowired
    private GenerateFactory generateFactory;
    @Test
    public void test() throws Exception {
        GenerateRequestDTO requestDTO = new GenerateRequestDTO();
        requestDTO.setRole("海绵宝宝");
        requestDTO.setStoryDesc("海绵宝宝去游泳");

        Story story = generateFactory.storyMaker(requestDTO);
        Story s = ollamaDeepSeekTextGenerate.generate(story.getStorySystemMessage(), story.getStoryUserMessage());
        System.out.println(s.getTitle());
        for (Story.Scene scene : s.getScenes()) {
            System.out.println(scene.getSceneTitle());
            System.out.println(scene.getSceneDesc());
            System.out.println(scene.getCaption());
        }
    }
}
