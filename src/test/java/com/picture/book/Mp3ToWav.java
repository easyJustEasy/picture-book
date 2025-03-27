package com.picture.book;

import com.zhuzhu.picturebook.generate.video.VideoGenerate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Mp3ToWav {
    public static void main(String[] args) throws IOException, InterruptedException {
     wavToMp3(new File("E:\\work\\picture-book\\voice\\asset\\longtong\\prompt.wav"));
    }
    public static void mp3ToWav() throws IOException, InterruptedException {
        File file = new File("E:\\work\\picture-book\\voice\\asset");
        for (File listFile : file.listFiles()) {
            File first = Arrays.stream(listFile.listFiles()).filter(e -> e.getName().endsWith(".mp3")).findFirst().orElse(null);
            if (first==null) {
                continue;
            }
            VideoGenerate videoGenerate = new VideoGenerate();
            videoGenerate.mp3ToWav(first.getAbsolutePath());
        }
    }
    public static void wavToMp3() throws IOException, InterruptedException {
        File file = new File("E:\\work\\picture-book\\voice\\asset");
        for (File listFile : file.listFiles()) {
            File first = Arrays.stream(listFile.listFiles()).filter(e -> e.getName().endsWith(".wav")).findFirst().orElse(null);
            if (first==null) {
                continue;
            }
            VideoGenerate videoGenerate = new VideoGenerate();
            videoGenerate.wavToMp3(first.getAbsolutePath());
        }
    }
    public static void wavToMp3(File wav) throws IOException, InterruptedException {
        VideoGenerate videoGenerate = new VideoGenerate();
        videoGenerate.wavToMp3(wav.getAbsolutePath());
    }
}
