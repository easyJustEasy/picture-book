package com.picture.voice;

import com.picture.book.PlayWav;
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
                """;
        String generate = remoteVoiceGenerate.generate(prompt, "海绵宝宝", 1f, "temp" + File.separator + UUID.randomUUID() + ".wav");
        new PlayWav(generate);
    }
}
