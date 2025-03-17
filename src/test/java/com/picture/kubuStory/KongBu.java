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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.Files.exists;

@SpringBootTest(classes = PictureBookApp.class)
public class KongBu {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;

    @Test
    void contextLoads1() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        File dir = new File("E:\\直播\\茅山轨道\\kongbu\\gushi");
        int length = Arrays.stream(Objects.requireNonNull(dir.listFiles())).filter(e -> e.getName().endsWith(".txt")).toList().size();
        CountDownLatch countDownLatch = new CountDownLatch(length);
        for (int i = 0; i < length; i++) {
            int finalI = i;
            String successFile = String.format("%s_output_part_success.wav", finalI);
            File successFileName = new File(dir.getAbsolutePath() + File.separator + successFile);
            if (successFileName.exists()) {
                countDownLatch.countDown();
                continue;
            }
            executorService.submit(() -> {
                String s = FileUtil.readString(String.format("%s" + File.separator + "%s_output_part.txt", dir.getAbsolutePath(), finalI), StandardCharsets.UTF_8);
                String fileName = dir.getAbsolutePath() + File.separator + String.format("%s_output_part.txt", finalI).replaceAll(".txt", "") + ".wav";
                try {
                    remoteVoiceGenerate.generate(s, "民间故事666", 1.0F, fileName);
                    FileUtil.rename(new File(fileName), successFile, true);
                    System.out.println("file is success ====>" + successFileName.getAbsolutePath());
                } catch (Exception e) {
                    FileUtil.del(fileName);
                    throw new RuntimeException(e);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("handle end=>>>>>>>>");

    }

    @Test
    void contextLoads2() {
        String s = FileUtil.readString("E:\\work\\picture-book\\src\\test\\resources\\taiyi\\1.txt", StandardCharsets.UTF_8);
        File temp = new File("temp");
        if (!temp.exists()) {
            temp.mkdir();
        }
        String fileName = temp.getAbsolutePath() + File.separator + s.substring(0, 20) + ".wav";
        try {
            String kehu = remoteVoiceGenerate.generate(s, "太乙真人", 1.0F, fileName);
            System.out.println("file is success ====>" + kehu);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
