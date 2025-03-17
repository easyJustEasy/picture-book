package com.zhuzhu.picturebook.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zhuzhu.picturebook.dto.GenerateResultDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.imgage.AbstractImageGenerate;
import com.zhuzhu.picturebook.generate.imgage.IImageGenerate;
import com.zhuzhu.picturebook.generate.imgage.ImageGenerateFactory;
import com.zhuzhu.picturebook.generate.text.ITextGenerate;
import com.zhuzhu.picturebook.generate.text.TextGenerateFactory;
import com.zhuzhu.picturebook.generate.voice.IVoiceGenerate;
import com.zhuzhu.picturebook.generate.voice.VoiceGenerateFactory;
import lombok.extern.slf4j.Slf4j;
import com.zhuzhu.picturebook.config.AiConfig;
import com.zhuzhu.picturebook.config.AppConfig;
import com.zhuzhu.picturebook.consts.GenerateStatus;
import com.zhuzhu.picturebook.dto.PictureDTO;
import com.zhuzhu.picturebook.generate.video.VideoGenerate;
import com.zhuzhu.picturebook.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractPictureBookService {
    @Autowired
    private VideoGenerate videoGenerate;
    @Autowired
    private TextGenerateFactory textGenerateFactory;
    @Autowired
    private VoiceGenerateFactory voiceGenerateFactory;
    @Autowired
    private ImageGenerateFactory imageGenerateFactory;
    @Autowired
    private AiConfig aiConfig;
    public static final String tail = """
            请生成一个故事，要求最少有6个场景，每个场景要有旁白和场景描述 每个场景用'@@@@@'隔开，结构为：’
            @@@@@
            故事标题：xxx
                                    
            @@@@@
                                    
            场景1
            旁白：xxx
            场景描述：xxx
                                    
            @@@@@
                                    
            场景2
            旁白：xxx
            场景描述：xxx
                                    
            @@@@@
            故事结束‘
            """;

    /**
     * 生成绘本故事
     *
     * @param actors    角色
     * @param system    生成故事框架的系统提示词
     * @param storyDesc 生成故事的用户提示词
     * @param workDir   工作目录
     * @return 视频路径
     * @throws Exception 异常
     */
    public String generate(String actors,
                           String system,
                           String role,
                           String storyDesc,
                           GenerateResultDTO dto,
                           String workDir,
                           GenerateCallBack callBack) {
        String systemMessage = StrUtil.replace(system, "%s", role);
        String userMessage = storyDesc + tail;
        dto.setStorySystemMessage(systemMessage);
        dto.setStoryUserMessage(userMessage);
        dto.setStatus(GenerateStatus.txt_ing.getDesc());
        callBack.process(dto);
        String s = null;
        try {
            s = generateFrame(systemMessage, userMessage);
        } catch (Exception e) {
            dto.setError(e.getMessage());
            dto.setStatus(GenerateStatus.txt_fail.getDesc());
            log.error(ExceptionUtil.stacktraceToString(e));
            callBack.process(dto);
            return null;
        }
        dto.setStoryOutputMessage(s);
        if (StrUtil.isBlankIfStr(s)) {
            dto.setStatus(GenerateStatus.txt_fail.getDesc());
            callBack.process(dto);
            return null;
        } else {
            dto.setStatus(GenerateStatus.txt_success.getDesc());
            callBack.process(dto);
        }
        Story story = Story.parseStory(s);
        //生成图片
        List<PictureDTO> list = new ArrayList<>();
        dto.setStatus(GenerateStatus.video_ing.getDesc());
        callBack.process(dto);
        for (Story.Scene scene : story.getScenes()) {
            PictureDTO pictureDTO = new PictureDTO(scene.getCaption(), scene.getSceneDesc(), workDir);
            try {
                pictureDTO.setImg(generatePictureDefault(actors, pictureDTO.getScene(), pictureDTO.getCaption(), workDir));
                pictureDTO.setVoice(generateVoice(pictureDTO.getCaption(), FileUtils.getUuidFileName(workDir, ".wav")));
                pictureDTO.setVideoPath(generateVideo(pictureDTO.getImg(), pictureDTO.getVoice(), workDir));

            } catch (Exception e) {
                dto.setError(e.getMessage());
                dto.setStatus(GenerateStatus.video_fail.getDesc());
                log.error(ExceptionUtil.stacktraceToString(e));
                callBack.process(dto);
                return null;
            }
            list.add(pictureDTO);
        }
        String concat = null;
        try {
            concat = concatVideo(list, workDir);
        } catch (Exception e) {
            dto.setError(e.getMessage());
            dto.setStatus(GenerateStatus.video_fail.getDesc());
            log.error(ExceptionUtil.stacktraceToString(e));
            callBack.process(dto);
            return null;
        }
        dto.setStatus(GenerateStatus.video_success.getDesc());
        callBack.process(dto);
        log.info("concat:" + concat);
        for (PictureDTO pictureDTO : list) {
            FileUtil.del(new File(pictureDTO.getImg()));
            FileUtil.del(new File(pictureDTO.getVoice()));
            FileUtil.del(new File(pictureDTO.getVideoPath()));
        }
        String voiceUrl = workDir + File.separator + story.getTitle() + ".mp4";
        FileUtil.rename(new File(concat), voiceUrl, true);
        try {
            FileUtil.move(new File(voiceUrl), new File(AppConfig.videoDir()), true);
            dto.setVideoUrl(AppConfig.videoUrl() + new File(voiceUrl).getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        dto.setStatus(GenerateStatus.success.getDesc());
        callBack.process(dto);
        return voiceUrl;
    }

    private String generatePictureDefault(String actors, String scene, String caption, String workDir) throws Exception {
        String path = generatePicture(actors, scene, caption, workDir);
        String newPath = AbstractImageGenerate.addCaption(path, caption, workDir);
        FileUtil.del(new File(path).getAbsolutePath());
        return new File(newPath).getAbsolutePath();
    }

    /**
     * 合并视频
     *
     * @param list    视频列表
     * @param workDir
     * @return 视频的路径
     */
    String concatVideo(List<PictureDTO> list, String workDir) throws Exception {
        return videoGenerate.concat(list, workDir);
    }

    /**
     * 生成语音
     *
     * @param caption 语音文本
     * @return 语音的路径
     */


    abstract String generateVoice(String caption, String workDir) throws Exception;

    /**
     * 生成故事框架
     *
     * @param system    生成故事的系统提示词
     * @param storyDesc 生成故事的用户提示词
     * @return 故事
     */
    abstract String generateFrame(String system, String storyDesc) throws Exception;

    /**
     * 生成图片
     *
     * @param actors  角色
     * @param scene   场景
     * @param caption 旁白
     * @return 图片路径
     */

    abstract String generatePicture(String actors, String scene, String caption, String workDir) throws Exception;

    /**
     * 生成视频
     *
     * @param img   图片
     * @param voice 声音
     * @return 视频路径
     */
    String generateVideo(String img, String voice, String workDir) throws Exception {
        return videoGenerate.generate(img, voice, workDir);
    }

    ITextGenerate getTextGenerate() {
        return textGenerateFactory.getGenerate(aiConfig.getText().getMode());
    }

    IVoiceGenerate getVoiceGenerate() {
        return voiceGenerateFactory.getGenerate(aiConfig.getVoice().getMode());
    }

    IImageGenerate getImageGenerate() {
        return imageGenerateFactory.getGenerate(aiConfig.getImage().getMode());
    }
}
