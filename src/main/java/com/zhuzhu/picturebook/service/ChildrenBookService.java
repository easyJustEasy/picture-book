package com.zhuzhu.picturebook.service;

import cn.hutool.core.util.StrUtil;
import com.zhuzhu.picturebook.consts.BookType;
import com.zhuzhu.picturebook.dto.GenerateResultDTO;
import com.zhuzhu.picturebook.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChildrenBookService extends AbstractPictureBookService implements IPictureBook {
    public static final String ACTORS = "";

    @Autowired
    private AppConfig appConfig;

    @Override
    String generateVoice(String caption, String filePath,String voice) throws Exception {
        if(StrUtil.isBlankIfStr(voice)){
            voice = "longtong";
        }
        return getVoiceGenerate().generate(caption, voice, 1.0F, filePath);
    }

    @Override
    String generateFrame(String system, String storyDesc) throws Exception {

        return getTextGenerate().generate(system, storyDesc);
    }

    @Override
    String generatePicture(String actors, String scene, String caption, String workDir) throws Exception {
        String prompt = makePrompt(actors, scene);
        return getImageGenerate().generate(prompt, workDir);
    }

    /**
     * 生成视频
     *
     * @param resultDTO 请求
     * @return 视频路径
     * @throws Exception 异常
     */

    @Override
    public String generate(GenerateResultDTO resultDTO, GenerateCallBack callBack) throws Exception {
        String role = resultDTO.getRole();
        String storyDesc = resultDTO.getStoryDesc();
        return generate(ACTORS, BookType.getSystem(resultDTO.getBookType()), role, storyDesc, resultDTO, appConfig.tempDir(), callBack);
    }
}
