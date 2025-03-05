package org.example.picturebook.generate.voice;

import org.example.picturebook.consts.GenerateMode;
import org.example.picturebook.generate.text.ITextGenerate;
import org.example.picturebook.generate.text.RemoteTextGenerate;
import org.example.picturebook.generate.text.TongYiTextGenerate;
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
