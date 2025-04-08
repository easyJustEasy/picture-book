package com.picture.book;

import cn.hutool.core.util.StrUtil;

import com.zhuzhu.picturebook.consts.BookType;
import com.zhuzhu.picturebook.covert.PictureBookConvert;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
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
    @Autowired
    TongYiTextGenerate tongYiTextGenerate;
    @Test
    public void test() throws Exception {
        GenerateRequestDTO requestDTO = new GenerateRequestDTO();
        requestDTO.setRole("海绵宝宝");
        requestDTO.setStoryDesc("海绵宝宝去游泳");
        String systemMessage =  StrUtil.replace(BookType.getSystem(1), "%s", requestDTO.getRole());
        String userMessage = requestDTO.getStoryDesc() + BookType.getTail(requestDTO.getBookType());
        String s = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
        Story story = Story.parseStory(s);
        System.out.println(story.getTitle());
        for (Story.Scene scene : story.getScenes()) {
            System.out.println(scene.getSceneTitle());
            System.out.println(scene.getSceneDesc());
            System.out.println(scene.getCaption());
        }
    }
    @Test
    public void test1() throws Exception {
        String systemMessage = """
                """;
        String userMessage = "生成关于海绵宝宝的20件事，要求事件符合逻辑，只需要事件的标题，不需要详细描述事件的经过,每个标题用@@@@@@@隔开只需要文字不需要序号";
        String s = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
        System.out.println(s);
    }

    @Test
    public void test2() throws Exception {
        String systemMessage =StrUtil.replace(BookType.getSystem(2),"%s","海绵宝宝");
        String userMessage = """
				相思
				红豆生南国，春来发几枝。
				愿君多采撷，此物最相思。
				""";
        String s = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
        System.out.println(s);
    }
    @Test
    public void test3() throws Exception {
        String systemMessage =StrUtil.replace(BookType.getSystem(1),"%s","海绵宝宝");
        String userMessage = """
                海绵宝宝的新家
                """;
        String s = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
        System.out.println(s);
    }
}
