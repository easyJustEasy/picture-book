package com.picture.img;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.config.AiConfig;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.consts.BookType;
import com.zhuzhu.picturebook.controller.GenerateImageController;
import com.zhuzhu.picturebook.covert.PictureBookConvert;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import com.zhuzhu.picturebook.service.AbstractPictureBookService;
import com.zhuzhu.picturebook.service.ChildrenBookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
        String systemMessage = StrUtil.replace(BookType.getSystem(requestDTO.getBookType()), "%s", requestDTO.getRole());
        String userMessage = requestDTO.getStoryDesc() + BookType.getTail(requestDTO.getBookType());
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

    @Autowired
    private OllamaDeepSeekTextGenerate textGenerate;

    @Autowired
    private RemoteVoiceGenerate voiceGenerate;
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private AppConfig appConfig;

    @Test
    void genImg() throws Exception {
        String parent = "temp" + File.separator + "img";
        FileUtil.mkdir(parent);
        remoteImageGenerate.generate("""
               海绵宝宝站在一片美丽的珊瑚礁旁，手中捧着一颗红豆。
                """, parent);
//        genGImg("红豆生南国,春来发几枝.愿君多采撷,此物最相思", parent);
    }

    private void genGImg(String prompt, String parent) throws Exception {
        String system = """
                  请根据用户输入的中国古诗，解释古诗，并按照古诗中描绘的意境生成绘制图片的提示词
                """;
        String generate = textGenerate.generate(system, prompt);
        String generate1 = remoteImageGenerate.generate(generate, parent);
        System.out.println(generate1);
    }

    @Test
    void scheduleImg() throws Exception {
        LocalDate today = LocalDate.now();
        String parent = "E:\\toutiaoimge";
        for (int i = 0; i < 30; i++) {
            String dir = parent + File.separator + today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File file = FileUtil.mkdir(dir);
            int size = 108 - Objects.requireNonNull(file.listFiles()).length;
            if (size < 0) {
                size = 0;
            }
            for (int j = 0; j < size; j++) {
                String prompt = "生成一个关于中国现代美女的图片提示词，要求皮肤白皙，形象可爱，只需要提示词，不要增加额外的信息";
                String system = """
                          请根据用户输入生成提示词，字数不得超过50个字，不需要输出额外的信息
                        """;
                prompt = """
                        请生成一个关于一个中国现代美女的提示词，要求年龄是18-30岁，皮肤白皙，形象可爱。
                        """;
                String generate = textGenerate.generate(system, prompt);
                remoteImageGenerate.generate(generate, dir);
            }
            today = today.plusDays(1);
        }
    }
}
