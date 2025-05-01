package com.picture.img;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.config.AiConfig;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.consts.BookType;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import com.zhuzhu.picturebook.service.ChildrenBookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        String caption = "海绵宝宝一大早就兴奋地起床了，今天他要去比奇堡最大的公共泳池玩水！画面里，海绵宝宝穿着五颜六色的泳裤，戴着可爱的泳帽，手里拿着救生圈，兴冲冲地走在比奇堡的大街上。阳光明媚，海浪轻轻拍打着沙滩，各种海洋生物纷纷向海绵宝宝打招呼。";
        String workDir = "temp";
        String s = childrenBookService.makePrompt(1, actors, scene);
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
        String generate = remoteImageGenerate.generate("""
                一个性感的中国美女，站在一个优美的风景里，快乐的向前伸手
                 """, parent);
        System.out.println(generate);
//        genGImg("红豆生南国,春来发几枝.愿君多采撷,此物最相思", parent);
    }
    @Test
    void genImg11() throws Exception {
        String parent = "temp" + File.separator + "img";
        File file = new File(parent);
        FileUtil.mkdirsSafely(file,3,1000);
        String generate = remoteImageGenerate.generate("""
            一位阳光活力的中国少女捧着一束鲜花，长相甜美可爱，皮肤白皙，身着粉色的半透明比基尼泳衣，露出甜蜜的微笑。场景是海边的白色沙滩，午后阳光在水晶般清澈的海面上洒下粼粼波光，
              整体画面采用柔焦摄影风格，色彩搭配低饱和莫兰迪粉色调，突出夏日浪漫氛围与青春活力。
                 """, file.getAbsolutePath());
        System.out.println(generate);
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
        String parent = "E:\\toutiaoimge1";
        if(!new File(parent).exists()){
            new File(parent).mkdir();
        }
        for (int i = 0; i < 60; i++) {
            String dir = parent + File.separator + today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File file = FileUtil.mkdir(dir);
            int size = 108 - Objects.requireNonNull(file.listFiles()).length;
            if (size < 0) {
                size = 0;
            }
            for (int j = 0; j < size; j++) {
                String generate = """
                        一位笑容甜美的中国少女，拥有瓷白透亮的肌肤，
                        穿着粉色半透明比基尼，手捧花束。
                        场景是午后阳光下的白色沙滩，
                        清澈海水泛着钻石般的波光。
                        柔焦摄影风格，
                        采用低饱和度的莫兰迪粉色调，
                        营造浪漫夏日氛围与青春活力。
                        """;
                remoteImageGenerate.generate(generate, dir);

            }
            today = today.plusDays(1);
        }
    }
    @Test
    void beauty() throws Exception {
        String actors = "";
        String scene = "画面里，yi";
        String caption = "愿你在未来的日子里，每天都能感受到进步与成长的喜悦，成为最闪耀的自己！";
        String workDir = "temp";
        String s = childrenBookService.makePrompt(1, actors, scene);
        String temp = remoteImageGenerate.generate(s, workDir);
        String newPath = addCaption(temp, caption, workDir);
        FileUtil.del(new File(temp).getAbsolutePath());
        System.out.println(newPath);

    }
}
