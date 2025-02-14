package com.picture.book.generate.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.picture.book.config.AppConfig;
import com.picture.book.consts.BookConsts;
import com.picture.book.dto.PageResult;
import com.picture.book.dto.PictureDTO;
import com.picture.book.dto.PictureResultDTO;
import com.picture.book.dto.Story;
import com.picture.book.generate.*;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_videoio.IVideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PictureBookGenerate implements IPictureBookGenerate {
    @Autowired GenerateFactory factory;
    public PictureResultDTO generate(String actors, Story story) throws Exception {
        if(story==null){
            throw new RuntimeException("故事情节为空，不能生成绘本");
        }
        PictureResultDTO pictureResultDTO = new PictureResultDTO();
        //生成图片
        List<PictureDTO> list = new ArrayList<>();
        for (Story.Scene scene : story.getScenes()) {
            PictureDTO pictureDTO = new PictureDTO(scene.getCaption(), scene.getSceneDesc());
            list.add(pictureDTO);
            pictureDTO.setImg(factory.imageGenerate().generate(actors, pictureDTO.getScene(), pictureDTO.getCaption()));
            pictureDTO.setVoice(factory.voiceGenerate().generate(pictureDTO.getCaption()));
            pictureDTO.setVideoPath(factory.videoGenerate().generate(pictureDTO.getImg(), pictureDTO.getVoice()));
        }
        String concat = factory.videoGenerate().concat(list);
        log.info("concat:" + concat);
        for (PictureDTO pictureDTO : list) {
            FileUtil.del(new File(pictureDTO.getImg()));
            FileUtil.del(new File(pictureDTO.getVoice()));
            FileUtil.del(new File(pictureDTO.getVideoPath()));
        }
        String s = AppConfig.videoDir() + File.separator + story.getTitle() + ".mp4";
        FileUtil.rename(new File(concat), s, true);
        pictureResultDTO.setPictureUrl(s);
        return pictureResultDTO;
    }
}
