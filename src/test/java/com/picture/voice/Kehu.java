package com.picture.voice;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import com.zhuzhu.picturebook.generate.voice.TongYiVoiceGenerate;
import com.zhuzhu.picturebook.third.audio.AudioPlayer;
import com.zhuzhu.picturebook.util.StringUtil;
import com.zhuzhu.picturebook.util.TitleUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;

@SpringBootTest(classes = PictureBookApp.class)
public class Kehu {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;
    @Autowired
    private TongYiVoiceGenerate tongYiVoiceGenerate;

    @Test
    void contextLoads() {
        String baseDir = "kehu1";
        File baseFile = FileUtil.mkdir(new File(baseDir));

        String s = FileUtil.readString("E:\\work\\picture-book\\src\\test\\resources\\kehu1\\1.txt", StandardCharsets.UTF_8);
        if (StrUtil.isBlankIfStr(s)) {
            throw new RuntimeException("初始化失败");
        }
        //分割章节
        String[] zhangjie = s.split("############");
        int titleCount = 1;
        for (String s1 : zhangjie) {
            if (StrUtil.isBlankIfStr(s1)) {
                continue;
            }
            //分割标题和段落
            String[] split = s1.split("#@@@#");
            String title = (titleCount++) + "-" + TitleUtil.sub(split[0]);
            if (StrUtil.isBlankIfStr(title)) {
                throw new RuntimeException("title 为空");
            }
            File titleDir = FileUtil.mkdir(baseFile.getAbsolutePath() + File.separator + title);
            String s2 = split[1];
            String[] duanluo = s2.split("@@@@@@");
            int duanluoCount = 1;
            for (String s3 : duanluo) {
                if (StrUtil.isBlankIfStr(s3)) {
                    continue;
                }
                //生成音频
                String name = titleDir.getAbsolutePath() + File.separator + (duanluoCount++) + "-" + TitleUtil.sub(StringUtil.replaceLineSeparator(s3));
                String fileName = name + ".wav";
                try {
                    String kehu = remoteVoiceGenerate.generate(s3, "kehu2", 1.0F, fileName);
                    File txt = FileUtil.touch(name + ".txt");
                    FileUtil.writeString(s3, txt.getAbsolutePath(), StandardCharsets.UTF_8);
                    System.out.println("file is success ====>" + kehu);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
//                if(titleCount==2){
//                    return;
//                }
            }
        }


    }

    @Test
    void contextLoads3() {
        String[] ss = {
                "欢迎新来的小伙伴们！很高兴见到你们，希望你们能在这里找到快乐，让我们一起度过美好的时光吧！",
                "哇，看到这么多熟悉的面孔，感觉特别温暖。老朋友们，感谢你们的一路相伴；新朋友们，期待与你们创造更多美好回忆。",
                "大家好呀！这里是啊焙焙的小天地，不论你是第一次来还是忠实观众，都欢迎你加入我们的大家庭！",
                "看到有新的小伙伴加入了我们，真的超级开心！别害羞，快来和我们一起聊天互动吧，让直播间的气氛更加热闹！",
                "每一位进入直播间的你都是我的小确幸，感谢你们的支持与陪伴。让我们开启今天的精彩旅程吧！",
                "亲爱的观众们，欢迎来到直播间，今天准备了很多有趣的内容等着和大家分享，希望每个人都能玩得开心！",
                "新朋友老朋友，大家都是一家人！不管何时何地，只要打开这个直播间，我们就能够相聚在一起，分享彼此的故事。",
                "尊敬的各位，感谢您选择在众多直播中来到了这里。愿我们在接下来的时间里，共同创造难忘的记忆！",
                "欢迎各位烘焙爱好者来到今天的直播！希望今天能给大家带来不一样的甜蜜体验。",
                "大家好呀！很高兴在这个美好的时刻与你们相遇，准备好了吗？让我们一起开启今天的美味之旅吧！",
                "亲爱的朋友们，感谢你们再次光临我们的直播间。今天我为大家准备了一些特别的惊喜，千万不要错过哦！",
                "看到这么多熟悉的面孔，真让人感到温暖！欢迎大家的到来，今天我们将分享一些超级好吃的蛋糕。",
                "嗨，甜品爱好者们！欢迎来到我的直播间，今天我们将一起探索制作令人垂涎三尺的蛋糕艺术，期待与你们互动！",
                "欢迎来到甜蜜的蛋糕世界！不管你是烘焙新手还是高手，今天都有很多有趣的内容等着你来发现。",
                "非常高兴见到每一位热爱生活的你！让我们一起享受这难得的相聚时光，共同创造属于我们的美味记忆。",
                "亲爱的观众朋友们，欢迎回到我们的直播！希望今天的分享能为你的生活增添一份甜蜜和快乐。",
                "大家好，欢迎来到今天的蛋糕私房直播！希望今天的分享能给你带来满满的灵感和甜蜜。",
                "欢迎各位甜品探索者！感谢你们的到来，今天我们将一起揭开美味蛋糕背后的秘密。",
                "亲爱的朋友们，欢迎再次相聚在这里。让我们用烘焙连接彼此，分享每一份甜蜜。",
                "嗨，美食爱好者们！欢迎加入我们今天的甜蜜聚会，准备好一起享受烘焙的乐趣了吗？",
                "看到这么多热情的面孔真让人兴奋！欢迎大家的到来，让我们一起动手制作美味蛋糕吧。",
                "您好，甜点达人！欢迎来到我的直播间，今天我们有特别的蛋糕等着你来发现哦。",
                "亲爱的观众朋友们，很高兴在这个美好的日子与您相会。愿今日的直播带给您无限的甜蜜与惊喜。",
                "欢迎来到这个充满爱与创意的空间！无论你是老朋友还是新面孔，今天都将是一次美妙的体验。",
                "感谢大家在百忙之中抽出时间来到我的直播间，今天我会尽全力让大家感受到烘焙的魅力。",
                "亲爱的朋友们，欢迎你们！今天我们分享的这款蛋糕，绝对会让你惊艳不已，请期待吧！",
                "欢迎各位美食探险家来到今天的直播间！让我们一起探索蛋糕制作的无限可能吧。",
                "亲爱的朋友们，很高兴能在这个美好的时刻遇见你。今天我们将一起体验一场甜蜜的烘焙之旅。",
                "大家好！感谢你们的支持与陪伴，今天的直播一定会充满惊喜。",
                "热烈欢迎所有爱好甜点的朋友们！今天我准备了一些小秘密要和大家分享，期待吗？",
                "欢迎来到这个温馨的小角落，这里是每个甜品爱好者的乐园。让我们一起享受这美妙的时光吧。",
                "大家好呀！非常高兴见到这么多热爱生活的你。今天我们将一起创造属于我们的甜蜜记忆。",
                "感谢每一位支持我的朋友，欢迎再次回到我的直播间。今天我们有更多有趣的内容等着你哦。",
                "亲爱的观众们，欢迎来到今天的直播课堂，让我们在甜蜜的世界里畅游，学习新技能。",
                "咱们是开封这边的，做蛋糕私房定制的哈，有需要定制蛋糕的可以私信哈",
                "咱们是纯手动制作，可选动物奶油和乳汁奶油哦，绝无任何添加剂哦",
        };
        for (String s : ss) {
            String s1 =   generageSingle(s,"kehu");
            AudioPlayer.playSound(s1);
        }

    }


    @Test
    void contextLoads1() {
        String[] ss = {
                """
         海绵宝宝一大早就兴奋地起床了，今天他要去比奇堡最大的公共泳池玩水！
                        """,
        };
        for (String s : ss) {
            String s1 = generageSingle(s,"海绵宝宝");
            AudioPlayer.playSound(s1);
        }

    }

    public String generageSingle(String s,String role) {
        File temp = new File("temp");
        if (!temp.exists()) {
            temp.mkdir();
        }
        String fileName = temp.getAbsolutePath() + File.separator + StrUtil.sub(StringUtil.replaceLineSeparatorToBlank(s), 0, 20) + ".wav";
        try {
            String kehu = remoteVoiceGenerate.generate(s,role , 1.0F, fileName);
            System.out.println("file is success ====>" + kehu);
            return kehu;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
