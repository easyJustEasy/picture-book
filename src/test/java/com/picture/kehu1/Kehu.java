package com.picture.kehu1;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.bytedeco.opencv.presets.opencv_core;
import org.example.PictureBookApp;
import org.example.picturebook.dto.Story;
import org.example.picturebook.generate.voice.RemoteVoiceGenerate;
import org.example.picturebook.util.StringUtil;
import org.example.picturebook.util.TitleUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest(classes = PictureBookApp.class)
public class Kehu {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;

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
    void contextLoads1() {
        String[] ss = {"欢迎新来的小伙伴们！很高兴见到你们，希望你们能在这里找到快乐，让我们一起度过美好的时光吧！",
                "哇，看到这么多熟悉的面孔，感觉特别温暖。老朋友们，感谢你们的一路相伴；新朋友们，期待与你们创造更多美好回忆。",
                "大家好呀！这里是大道至简的小天地，不论你是第一次来还是忠实观众，都欢迎你加入我们的大家庭！",
                "看到有新的小伙伴加入了我们，真的超级开心！别害羞，快来和我们一起聊天互动吧，让直播间的气氛更加热闹！",
                "每一位进入直播间的你都是我的小确幸，感谢你们的支持与陪伴。让我们开启今天的精彩旅程吧！",
                "亲爱的观众们，欢迎来到直播间，今天准备了很多有趣的内容等着和大家分享，希望每个人都能玩得开心！",
                "新朋友老朋友，大家都是一家人！不管何时何地，只要打开这个直播间，我们就能够相聚在一起，分享彼此的故事。",
                "尊敬的各位，感谢您选择在众多直播中来到了这里。愿我们在接下来的时间里，共同创造难忘的记忆！"};
        for (String s : ss) {
            File temp = new File("temp");
            if (!temp.exists()) {
                temp.mkdir();
            }
            String fileName = temp.getAbsolutePath() + File.separator + StrUtil.sub(s, 0, 20) + ".wav";
            try {
                String kehu = remoteVoiceGenerate.generate(s, "女主播", 1.0F, fileName);
                System.out.println("file is success ====>" + kehu);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
