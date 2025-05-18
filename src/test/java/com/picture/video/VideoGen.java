package com.picture.video;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.picture.book.PlayWav;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.video.VideoGenerate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@SpringBootTest(classes = PictureBookApp.class)
public class VideoGen {
    @Autowired
    private VideoGenerate videoGenerate;

    @Test
    public void test() throws Exception {
        String parent = "E:\\toutiaoimge1";
        String record = "E:\\toutiaoimge1\\record.txt";
        FileUtil.touch(record);
        String s = FileUtil.readString(record, StandardCharsets.UTF_8);
        File lastRecordFile = StrUtil.isBlankIfStr(s) ? null : new File(s);
        File lastRecordParentFile = null;
        if (lastRecordFile != null) {
            lastRecordParentFile = lastRecordFile.getParentFile();
        }
        log.info("start image dir is " + parent);
        String audio = "E:\\music\\小美满.mp3";
        File file = new File(parent);
        File workDir = new File("temp");
        int i = 0;
        List<File> images = new ArrayList<>();
        File lastFile = null;
        boolean matched = lastRecordParentFile == null;
        boolean started = lastRecordFile==null;
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (Objects.equals(listFile, lastRecordParentFile)) {
                matched = true;
            }
            if (!matched) {
                continue;
            }
            File[] files = listFile.listFiles();
            if (files==null) {
                continue;
            }
            for (File file1 : files) {
                if (Objects.equals(file1, lastRecordFile)) {
                    started = true;
                }
                if (started) {
                    lastFile = file1;
                    images.add(file1);
                    if (i++ >= 215) {
                        videoGenerate.generate(images, audio, workDir.getAbsolutePath());
                        FileUtil.writeUtf8String(lastFile.getAbsolutePath(), record);
                        images = new ArrayList<>();
                        i = 0;
                    }
                }

            }

        }
    }
}
