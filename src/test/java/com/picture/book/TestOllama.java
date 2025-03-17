package com.picture.book;

import cn.hutool.core.util.StrUtil;

import lombok.extern.slf4j.Slf4j;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.service.AbstractPictureBookService;
import com.zhuzhu.picturebook.service.ChildrenBookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PictureBookApp.class)
@Slf4j
public class TestOllama {
    @Autowired
    OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;

    @Test
    public void test() throws Exception {
        GenerateRequestDTO requestDTO = new GenerateRequestDTO();
        requestDTO.setRole("海绵宝宝");
        requestDTO.setStoryDesc("海绵宝宝去游泳");
        String systemMessage =  StrUtil.replace(ChildrenBookService.system, "%s", requestDTO.getRole());
        String userMessage = requestDTO.getStoryDesc() + AbstractPictureBookService.tail;
        String s = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
        Story story = Story.parseStory(s);
        System.out.println(story.getTitle());
        for (Story.Scene scene : story.getScenes()) {
            System.out.println(scene.getSceneTitle());
            System.out.println(scene.getSceneDesc());
            System.out.println(scene.getCaption());
        }
    }
}
