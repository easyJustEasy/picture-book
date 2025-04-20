package com.picture.voice;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MicrophoneRecorder {
    // 定义音频格式
    AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
    TargetDataLine targetLine;

    public void startRecording() throws LineUnavailableException {
        targetLine = AudioSystem.getTargetDataLine(format);
        targetLine.open(format);
        targetLine.start();

        // 创建音频输入流
        AudioInputStream audioInputStream = new AudioInputStream(targetLine);

        // 定义输出文件
        File audioFile = new File("recordedAudio.wav");

        // 保存音频文件
        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
            System.out.println("Recording...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        targetLine.stop();
        targetLine.close();
        System.out.println("Recording stopped.");
    }

    public static void main(String[] args) {
        MicrophoneRecorder recorder = new MicrophoneRecorder();
        try {
            recorder.startRecording();
            // 录制5秒钟
            Thread.sleep(5000);
            recorder.stopRecording();
        } catch (InterruptedException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
