package com.picture.img;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import com.zhuzhu.picturebook.service.AbstractPictureBookService;
import com.zhuzhu.picturebook.service.ChildrenBookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static com.zhuzhu.picturebook.generate.imgage.AbstractImageGenerate.addCaption;

@SpringBootTest(classes = PictureBookApp.class)
public class ImgTest {
    @Autowired
    private RemoteImageGenerate remoteImageGenerate;
    @Autowired
    private TongYiTextGenerate tongYiTextGenerate;

    @Autowired
    private ChildrenBookService childrenBookService;

    @Test
    void textGen() throws Exception {
        GenerateRequestDTO requestDTO = new GenerateRequestDTO();
        requestDTO.setRole("海绵宝宝");
        requestDTO.setStoryDesc("海绵宝宝去游泳");
        String systemMessage = StrUtil.replace(ChildrenBookService.system, "%s", requestDTO.getRole());
        String userMessage = requestDTO.getStoryDesc() + AbstractPictureBookService.tail;
        String s = tongYiTextGenerate.generate(systemMessage, userMessage);
        Story story = Story.parseStory(s);
        System.out.println(story.getTitle());
        for (Story.Scene scene : story.getScenes()) {
            System.out.println(scene.getSceneTitle());
            System.out.println(scene.getSceneDesc());
            System.out.println(scene.getCaption());
        }

    }

    @Test
    void contextLoads() throws Exception {
        String actors = "";
        String scene = "画面里，海绵宝宝穿着五颜六色的泳裤，戴着可爱的泳帽，手里拿着救生圈，兴冲冲地走在比奇堡的大街上。阳光明媚，海浪轻轻拍打着沙滩，各种海洋生物纷纷向海绵宝宝打招呼。";
        String caption = "海绵宝宝一大早就兴奋地起床了，今天他要去比奇堡最大的公共泳池玩水！";
        String workDir = "temp";
        String s = childrenBookService.makePrompt(actors, scene);
        String temp = remoteImageGenerate.generate(s, workDir);
        String newPath = addCaption(temp, caption, workDir);
        FileUtil.del(new File(temp).getAbsolutePath());
        System.out.println(newPath);

    }
    @Test
    void giveAImageByText() throws Exception {

        String workDir = "temp";
        String s = """
                生成一个美女图片
                """;
        String temp = remoteImageGenerate.generate(s, workDir);
        System.out.println(temp);

    }
}
