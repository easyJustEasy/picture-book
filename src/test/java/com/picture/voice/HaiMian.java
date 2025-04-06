package com.picture.voice;

import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;

@SpringBootTest(classes = PictureBookApp.class)
public class HaiMian {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;

    @Test
    public void test() throws Exception {
        String prompt = """
                相思
                王维王维〔唐代〕
                                
                红豆生南国，春来发几枝。
                愿君多采撷，此物最相思。
                """;
        remoteVoiceGenerate.generate(prompt,"",1f,"temp"+ File.separator + UUID.randomUUID()+".wav");
    }
}
