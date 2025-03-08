package com.picture.book;

import org.example.picturebook.generate.video.VideoGenerate;
import org.opencv.video.Video;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class Mp3ToWav {
    public static void main(String[] args) throws IOException, InterruptedException {
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
}
