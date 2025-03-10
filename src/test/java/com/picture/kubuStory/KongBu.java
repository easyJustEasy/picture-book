package com.picture.kubuStory;

import cn.hutool.core.io.FileUtil;
import org.example.PictureBookApp;
import org.example.picturebook.generate.voice.RemoteVoiceGenerate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootTest(classes = PictureBookApp.class)
public class KongBu {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;
    @Test
    void contextLoads1() {
        File dir = new File("E:\\work\\picture-book\\src\\test\\resources\\kongbu\\gushi");
        for (File file : Arrays.stream(dir.listFiles()).filter(e->e.getName().endsWith(".txt")).toList()) {
            String s = FileUtil.readString(file.getAbsolutePath(), StandardCharsets.UTF_8);
            String fileName = dir.getAbsolutePath()+File.separator+file.getName().replaceAll(".txt","")+".wav";
            try {
                String kehu = remoteVoiceGenerate.generate(s, "民间故事666",1.0F, fileName);
                System.out.println("file is success ====>"+kehu);
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }
    @Test
    void contextLoads2() {
        String s = FileUtil.readString("E:\\work\\picture-book\\src\\test\\resources\\taiyi\\1.txt", StandardCharsets.UTF_8);
        File temp = new File("temp");
        if(!temp.exists()){
            temp.mkdir();
        }
        String fileName = temp.getAbsolutePath()+File.separator+s.substring(0,20)+".wav";
        try {
            String kehu = remoteVoiceGenerate.generate(s, "太乙真人",1.0F, fileName);
            System.out.println("file is success ====>"+kehu);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
