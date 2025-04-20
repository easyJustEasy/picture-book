package com.picture.voice;

import com.picture.book.PlayWav;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
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
    @Autowired
    private OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;

    @Test
    public void test() throws Exception {

        while(true){
            String prompt = ollamaDeepSeekTextGenerate.generate("""
                    你是一个主播，现在在卖一款涂鸦板，这个涂鸦板是LCD屏幕，8.5寸，护眼的，可用于办公室，儿童作画，带有意见消除功能，价格是7.7元。你要生成一个售卖话术
                    """, """
                    生成一个话术
                    """);
            String generate = remoteVoiceGenerate.generate(prompt, "海绵宝宝", 1f, "temp" + File.separator + UUID.randomUUID() + ".wav");
            new PlayWav(generate);
        }

    }
}
