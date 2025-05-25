package com.picture.book;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.consts.BookType;
import com.zhuzhu.picturebook.dto.BatchGenerateRequestDTO;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.RemoteTextGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import com.zhuzhu.picturebook.service.BookGenerateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = PictureBookApp.class)
class BookApplicationTests {

    @Test
    void contextLoads() {
        String s = """
                @@@@@
                     故事标题：海绵宝宝的快乐野餐
                     
                     @@@@@
                     
                     场景1 \s
                     旁白：海绵宝宝一大早就开始准备野餐的食物啦！他做了一大篮子章鱼酥、菠菜派和蟹黄三明治。 \s
                     场景描述：画面中海绵宝宝穿着围裙，在厨房里忙碌着，桌上摆满了各种美食。阳光透过窗户洒进来，显得温馨又明亮。
                     
                     @@@@@
                     
                     场景2 \s
                     旁白：海绵宝宝邀请派大星一起去野餐，他们决定去比奇堡的海滩公园。 \s
                     场景描述：海绵宝宝和派大星手牵手走在路上，路两旁是五彩缤纷的花朵，远处能看到蓝色的大海和沙滩。他们的篮子里装满了美味的野餐食物。
                     
                     @@@@@
                     
                     场景3 \s
                     旁白：他们来到公园后，发现珍珍已经铺好了漂亮的格子布，还摆放了一些水果。 \s
                     场景描述：珍珍站在毯子旁边，笑眯眯地迎接他们。毯子上放着切好的西瓜、苹果和葡萄，看起来清爽可口。
                     
                     @@@@@
                     
                     场景4 \s
                     旁白：大家正吃得开心时，珊迪带着她的机器人朋友突然来访。 \s
                     场景描述：珊迪站在一旁，她的机器人朋友好奇地看着周围的一切。海绵宝宝热情地邀请它一起享用美食，机器人也开心地点头。
                     
                     @@@@@
                     
                     场景5 \s
                     旁白：大家玩起了沙滩排球，笑声充满了整个公园。 \s
                     场景描述：大家分成两组玩排球，派大星和机器人一组，珍珍和珊迪一组。海绵宝宝在场边欢呼，气氛热烈而欢乐。
                     
                     @@@@@
                     
                     场景6 \s
                     旁白：夕阳西下，大家一起收拾东西准备回家。 \s
                     场景描述：天边的晚霞映红了天空，大家依依不舍地告别。他们拎着空篮子，脸上洋溢着满足的笑容，慢慢走回家。
                     
                     @@@@@
                     
                     故事结束				""";
        Story story = Story.parseStory(s);
        System.out.println(JSONObject.toJSONString(story));
    }

    @Autowired
    BookGenerateService bookGenerateService;
    @Autowired
    OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;

    @Test
    public void test1() throws Exception {
        String systemMessage = """
                """;
        String userMessage = "生成关于海绵宝宝的50件事，要求事件符合逻辑，只需要事件的标题，不需要详细描述事件的经过,每个标题用@@@@@@@隔开只需要文字不需要序号";
        String desc = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
        String role = "海绵宝宝";

        List<GenerateRequestDTO> list = new ArrayList<>();
        String[] s = desc.split("@@@@@@@");
        for (String s1 : s) {
            if (StrUtil.isBlankIfStr(s1)) {
                continue;
            }
            GenerateRequestDTO generateRequestDTO = new GenerateRequestDTO();
            generateRequestDTO.setId(null);
            generateRequestDTO.setRole(role);
            generateRequestDTO.setStoryDesc(s1);
            list.add(generateRequestDTO);

        }
        BatchGenerateRequestDTO requestDTO = new BatchGenerateRequestDTO();
        requestDTO.setList(list);
        bookGenerateService.generateBatch(requestDTO, UUID.randomUUID().toString());
    }

    @Test
    public void test2() throws Exception {

        String desc = """
                王维：桃源行
                    
                渔舟逐水爱山春，两岸桃花夹古津。
                坐看红树不知远，行尽青溪不见人。
                山口潜行始隈隩，山开旷望旋平陆。
                遥看一处攒云树，近入千家散花竹。
                樵客初传汉姓名，居人未改秦衣服。
                居人共住武陵源，还从物外起田园。
                月明松下房栊静，日出云中鸡犬喧。
                惊闻俗客争来集，竞引还家问都邑。
                平明闾巷扫花开，薄暮渔樵乘水入。
                初因避地去人间，及至成仙遂不还。
                峡里谁知有人事？世中遥望空云山。
                不疑灵境难闻见，尘心未尽思乡县。
                出洞无论隔山水，辞家终拟长游衍。
                自谓经过旧不迷，安知峰壑今来变？
                当时只记入山深，青溪几曲到云林。
                春来遍是桃花水，不辨仙源何处寻。
                """;
        String role = "海绵宝宝";

        List<GenerateRequestDTO> list = new ArrayList<>();
        String[] s = desc.split("@@@@@@@");
        for (String s1 : s) {
            if (StrUtil.isBlankIfStr(s1)) {
                continue;
            }
            GenerateRequestDTO generateRequestDTO = new GenerateRequestDTO();
            generateRequestDTO.setId(null);
            generateRequestDTO.setRole(role);
            generateRequestDTO.setStoryDesc(s1);
            generateRequestDTO.setBookType(2);
            list.add(generateRequestDTO);

        }
        BatchGenerateRequestDTO requestDTO = new BatchGenerateRequestDTO();
        requestDTO.setList(list);
        bookGenerateService.generateBatch(requestDTO, UUID.randomUUID().toString());
    }

    @Test
    public void test3() throws Exception {
        String s = FileUtil.readString("E:\\work\\picture-book\\src\\test\\resources\\poem\\唐诗三百首.txt", StandardCharsets.UTF_8);
        String[] split = s.split("[0-9]+");
        String role = "海绵宝宝";
        List<GenerateRequestDTO> list = new ArrayList<>();
        for (String s1 : split) {
            if (StrUtil.isBlankIfStr(s1)) {
                continue;
            }
            if (s1.trim().length() < 10) {
                continue;
            }

            GenerateRequestDTO generateRequestDTO = new GenerateRequestDTO();
            generateRequestDTO.setId(null);
            generateRequestDTO.setRole(role);
            generateRequestDTO.setStoryDesc(s1);
            generateRequestDTO.setBookType(2);
            list.add(generateRequestDTO);

        }
        BatchGenerateRequestDTO requestDTO = new BatchGenerateRequestDTO();
        requestDTO.setList(list);
        bookGenerateService.generateBatch(requestDTO, UUID.randomUUID().toString());
    }

    @Autowired
    private OllamaDeepSeekTextGenerate remoteTextGenerate;

    @Test
    public void test4() throws Exception {
        List<GenerateRequestDTO> list = new ArrayList<>();
        try {
            for (int i = 0; i < 300; i++) {
                String prompt = remoteTextGenerate.generate("""
                        你是一个心灵鸡汤大师，擅长写作各种励志文案，听完能够振奋人心，激发人的斗志
                        """, """
                        生成一个励志文案
                        """);
                String role = "海绵宝宝";
                GenerateRequestDTO generateRequestDTO = new GenerateRequestDTO();
                generateRequestDTO.setId(null);
                generateRequestDTO.setRole(role);
                generateRequestDTO.setStoryDesc(prompt);
                generateRequestDTO.setBookType(BookType.CHICKEN_SOUP_FOR_THE_SOUL.getCode());
                list.add(generateRequestDTO);
            }
            BatchGenerateRequestDTO requestDTO = new BatchGenerateRequestDTO();
            requestDTO.setList(list);
            bookGenerateService.generateBatch(requestDTO, UUID.randomUUID().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}

