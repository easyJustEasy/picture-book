package org.example.picturebook.third.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {

    // 播放WAV音频文件的方法
    public static void playSound(String filePath) {
        try {
            // 定义一个音频输入流，从给定的文件路径加载音频数据
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));

            // 获取音频格式
            AudioFormat format = audioInputStream.getFormat();

            // 创建一个数据行信息对象
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            // 打开并开始播放音频
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
            clip.start();

            // 等待直到声音结束
            while (!clip.isRunning())
                Thread.sleep(10);
            while (clip.isRunning())
                Thread.sleep(10);

            // 关闭资源
            clip.close();
            audioInputStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 调用playSound方法并传入WAV文件的路径
        playSound("path/to/your/audiofile.wav");
    }
}