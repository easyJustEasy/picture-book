package com.picture.text;

import cn.hutool.core.util.StrUtil;
import com.picture.book.PlayWav;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.RemoteTextGenerate;
import com.zhuzhu.picturebook.generate.text.TextGenerateFactory;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import com.zhuzhu.picturebook.third.audio.AudioPlayer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@SpringBootTest(classes = PictureBookApp.class)
public class TextTest {

    @Autowired
    private RemoteTextGenerate remoteTextGenerate;
    @Autowired
    private TextGenerateFactory factory;

    @Test
    public void test() throws Exception {

        String prompt = remoteTextGenerate.generate("""
                你是一个主播，现在在卖一款涂鸦板，这个涂鸦板是LCD屏幕，8.5寸，护眼的，可用于办公室，儿童作画，带有意见消除功能，价格是7.7元。你要生成一个售卖话术
                """, """
                生成一个话术
                """);
        System.out.println(prompt);

    }

    @Test
    public void test1() throws Exception {

        String prompt = factory.getGenerate(2).generate("""
                你是一个主播，现在在卖一款涂鸦板，这个涂鸦板是LCD屏幕，8.5寸，护眼的，可用于办公室，儿童作画，带有意见消除功能，价格是7.7元。你要生成一个售卖话术
                """, """
                生成一个话术
                """);
        System.out.println(prompt);

    }
}
