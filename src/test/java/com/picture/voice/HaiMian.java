package com.picture.voice;

import cn.hutool.core.util.StrUtil;
import com.picture.book.PlayWav;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.imgage.RemoteImageGenerate;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import com.zhuzhu.picturebook.third.audio.AudioPlayer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootTest(classes = PictureBookApp.class)
public class HaiMian {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;
    @Autowired
    private TongYiTextGenerate tongYiTextGenerate;
    @Autowired
    private OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;
    @Autowired
    private RemoteImageGenerate imageGenerate;

    @Test
    public void test() throws Exception {

        while (true) {
            String prompt = ollamaDeepSeekTextGenerate.generate("""
                    你是一个主播，现在在卖一款涂鸦板，这个涂鸦板是LCD屏幕，8.5寸，护眼的，可用于办公室，儿童作画，带有意见消除功能，价格是7.7元。你要生成一个售卖话术
                    """, """
                    生成一个话术
                    """);
            String generate = remoteVoiceGenerate.generate(prompt, "海绵宝宝", 1f, "temp" + File.separator + UUID.randomUUID() + ".wav");

            new PlayWav(generate);
        }

    }

    @Test
    public void test2() throws Exception {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        Runnable run = ()->{
            while (true) {
                String prompt = null;
                try {
                    prompt = ollamaDeepSeekTextGenerate.generate("""
                            你是一个心灵鸡汤大师，擅长写作各种励志文案，听完能够振奋人心，激发人的斗志
                            """, """
                            生成一个励志文案
                            """);
                    String generate = remoteVoiceGenerate.generate(prompt+"，点赞收藏，明天中大奖，哈哈哈", "蒋介石", 1f, "temp" + File.separator + UUID.randomUUID() + ".wav");
//                    String generate1 = imageGenerate.generate("生成一个美丽的春日或者夏日风景图片", "temp");
                    System.out.println(prompt);
                    System.out.println(generate);
//                    System.out.println(generate1);
                    queue.add(generate);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }};
        new Thread(run).start();
            new Thread(run).start();
        new Thread(()->{
            while (true){
                try {
                    String take = queue.poll();
                    if(StrUtil.isBlankIfStr(take)){
                        continue;
                    }
                    AudioPlayer.playSound(take);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
        while (1==1){}

    }

    @Test
    public void test1() throws Exception {

        String generate = remoteVoiceGenerate.generate("""
                春天的事业是温暖的，夏天的事业是芬芳的，秋天的事业是沉甸甸的，冬天的事业是平静的。祝你事业有成，张扬生命的精彩！
                """, "海绵宝宝", 1f, "temp" + File.separator + UUID.randomUUID() + ".wav");
        new PlayWav(generate);
    }
}
