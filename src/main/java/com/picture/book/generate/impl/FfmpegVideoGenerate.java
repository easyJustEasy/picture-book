package com.picture.book.generate.impl;


import cn.hutool.core.io.FileUtil;
import com.picture.book.config.AppConfig;
import com.picture.book.dto.PictureDTO;
import com.picture.book.generate.IVideoGenerate;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Component
@Slf4j
public class FfmpegVideoGenerate implements IVideoGenerate {
    public  String generate(String imagesPath,String audioPath) throws Exception {
        String outputVideoPath = AppConfig.tempDir()+File.separator+ UUID.randomUUID()+".mp4";
        List<File> imageFiles = new ArrayList<>();
        imageFiles.add(new File(imagesPath));
        BufferedImage firstImage = ImageIO.read(imageFiles.get(0));
        int width = firstImage.getWidth();
        int height = firstImage.getHeight();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputVideoPath, width, height);
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFrameRate(2); // 每张图片显示一秒
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        try {
            recorder.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();

            for (File imageFile : imageFiles) {
                BufferedImage image = ImageIO.read(imageFile);
                Frame frame = converter.convert(image);
                recorder.record(frame);
            }
        } finally {
            recorder.stop();
            recorder.release();
        }
        String outputVideoPathFinal = AppConfig.tempDir()+File.separator+ UUID.randomUUID()+".mp4";

        // 合并音频到视频
        addAudioToVideo(audioPath, outputVideoPath, outputVideoPathFinal);
        FileUtil.del(new File(outputVideoPath).getAbsolutePath());
        return new File(outputVideoPathFinal).getAbsolutePath();
    }

    private  void addAudioToVideo(String audioFilePath, String videoWithoutAudioPath, String finalOutputPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg",
                "-i", videoWithoutAudioPath,
                "-i", audioFilePath,
                "-c:v", "copy",
                "-c:a", "aac",
                "-r","30",
                "-strict", "experimental",
                finalOutputPath);
        pb.inheritIO().start().waitFor();
    }

    public  String concat(List<PictureDTO> list) throws Exception {
        String property = AppConfig.tempDir();
        String filelist = property+File.separator+UUID.randomUUID()+".txt";
        FileUtil.touch(new File(filelist));
        FileUtil.writeLines(list.stream().map(s -> "file '" + s.getVideoPath() + "'").toList(), new File(filelist), "utf-8");

        String outputVideo = property+File.separator+"output_"+UUID.randomUUID()+".mp4";
        ProcessBuilder pb = new ProcessBuilder("ffmpeg",
//                "-v","debug",
                "-r","30",
                "-f", "concat",
                "-safe", "0",
                "-i", new File(filelist).getAbsolutePath(),
                "-c", "copy",
                outputVideo);
        pb.inheritIO().start().waitFor();
        FileUtil.del(new File(filelist).getAbsolutePath());
        return outputVideo;
    }
}
