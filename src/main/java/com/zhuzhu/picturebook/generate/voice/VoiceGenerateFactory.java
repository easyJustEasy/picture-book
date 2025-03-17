package com.zhuzhu.picturebook.generate.voice;

import com.zhuzhu.picturebook.consts.GenerateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class VoiceGenerateFactory {
    @Autowired
    private ApplicationContext springContext;

    public IVoiceGenerate getGenerate(int mode) {
        switch (GenerateMode.getByCode(mode)) {
            case REMOTE_API:
                return springContext.getBean(RemoteVoiceGenerate.class);
            default:
                return springContext.getBean(TongYiVoiceGenerate.class);
        }
    }
}
