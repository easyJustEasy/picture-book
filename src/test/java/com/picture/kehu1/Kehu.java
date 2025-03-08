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

        String s  = FileUtil.readString("E:\\work\\picture-book\\src\\test\\resources\\kehu1\\1.txt", StandardCharsets.UTF_8);
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
            String title = (titleCount++)+"-"+TitleUtil.sub(split[0]);
            if (StrUtil.isBlankIfStr(title)) {
                throw new RuntimeException("title 为空");
            }
            File titleDir = FileUtil.mkdir(baseFile.getAbsolutePath()+File.separator+title);
            String s2 = split[1];
            String[] duanluo = s2.split("@@@@@@");
            int duanluoCount = 1;
            for (String s3 : duanluo) {
                if (StrUtil.isBlankIfStr(s3)) {
                    continue;
                }
                //生成音频
                String name =  titleDir.getAbsolutePath()+File.separator+ (duanluoCount++)+"-"+TitleUtil.sub(StringUtil.replaceLineSeparator(s3));
                String fileName =name+".wav";
                try {
                    String kehu = remoteVoiceGenerate.generate(s3, "kehu2",1.0F, fileName);
                    File txt = FileUtil.touch(name+".txt");
                    FileUtil.writeString(s3,txt.getAbsolutePath(),StandardCharsets.UTF_8);
                    System.out.println("file is success ====>"+kehu);
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
        String s = """
哈喽，亲爱的抖音小伙伴们！新进来直播间的朋友，动动手指，左上角点个免费的关注，别错过这场电脑租赁的超值盛宴。咱直播间专业深耕电脑租赁领域，致力于打破高成本用机的壁垒，让每一位朋友无需砸下重金，就能轻松拥抱高性能电脑，性价比直接拉满，精彩内容马上呈现，快跟上节奏哟！                                
                """;
        File temp = new File("temp");
        if(!temp.exists()){
            temp.mkdir();
        }
        String fileName = temp.getAbsolutePath()+File.separator+s.substring(0,20)+".wav";
        try {
            String kehu = remoteVoiceGenerate.generate(s, "kehu2",1.0F, fileName);
            System.out.println("file is success ====>"+kehu);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
