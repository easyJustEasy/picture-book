package org.example.picturebook.generate.video;


import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.example.picturebook.dto.PictureDTO;
import org.example.picturebook.config.AppConfig;
import org.springframework.stereotype.Component;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class VideoGenerate {
    public String generate(String imagesPath, String audioPath, String workDir) throws Exception {
        String outputVideoPath = workDir + File.separator + UUID.randomUUID() + ".mp4";
        List<File> imageFiles = new ArrayList<>();
        imageFiles.add(new File(imagesPath));
        BufferedImage firstImage = ImageIO.read(imageFiles.get(0));
        int width = firstImage.getWidth();
        int height = firstImage.getHeight();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputVideoPath, width, height);
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFrameRate(1); // 每张图片显示一秒
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
        String outputVideoPathFinal = workDir + File.separator + UUID.randomUUID() + ".mp4";

        // 合并音频到视频
        addAudioToVideo(audioPath, outputVideoPath, outputVideoPathFinal);
        FileUtil.del(new File(outputVideoPath).getAbsolutePath());
        return new File(outputVideoPathFinal).getAbsolutePath();
    }

    private void addAudioToVideo(String audioFilePath, String videoWithoutAudioPath, String finalOutputPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg",
                "-i", videoWithoutAudioPath,
                "-i", audioFilePath,
                "-c:v", "copy",
                "-c:a", "aac",
                "-r", "30",
                "-strict", "experimental",
                finalOutputPath);
        pb.inheritIO().start().waitFor();
    }

    public String concat(List<PictureDTO> list, String workDir) throws Exception {
        String filelist = workDir + File.separator + UUID.randomUUID() + ".txt";
        FileUtil.touch(new File(filelist));
        FileUtil.writeLines(list.stream().map(s -> "file '" + s.getVideoPath() + "'").toList(), new File(filelist), "utf-8");

        String outputVideo = workDir + File.separator + "output_" + UUID.randomUUID() + ".mp4";
        ProcessBuilder pb = new ProcessBuilder("ffmpeg",
//                "-v","debug",
                "-r", "30",
                "-f", "concat",
                "-safe", "0",
                "-i", new File(filelist).getAbsolutePath(),
                "-c", "copy",
                outputVideo);
        pb.inheritIO().start().waitFor();
        FileUtil.del(new File(filelist).getAbsolutePath());
        return outputVideo;
    }

    public String mp3ToWav(String filelist) throws IOException, InterruptedException {
        File file = new File(filelist);
        String outWav = file.getParent()+File.separator+ file.getName().replaceAll(".mp3","") + ".wav";
        ProcessBuilder pb = new ProcessBuilder("ffmpeg",
//                "-v","debug",
                "-i", file.getAbsolutePath(),
                "-acodec", "pcm_s16le",
                "-ac", "2",
                "-ar", "44100",
                outWav);
        pb.inheritIO().start().waitFor();
        return outWav;

    }
    public String wavToMp3(String filelist) throws IOException, InterruptedException {
        File file = new File(filelist);
        String outWav = file.getParent()+File.separator+ file.getName().replaceAll(".wav","") + ".mp3";
        ProcessBuilder pb = new ProcessBuilder("ffmpeg",
//                "-v","debug",
                "-i", file.getAbsolutePath(),
                "-b:a", "64k",
                "-acodec", "mp3",
                "-ar", "44100",
                "-ac","1",
                outWav);
        pb.inheritIO().start().waitFor();
        return outWav;

    }
}
