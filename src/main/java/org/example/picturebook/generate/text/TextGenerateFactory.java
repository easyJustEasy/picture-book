package org.example.picturebook.generate.text;

import org.example.picturebook.consts.GenerateMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TextGenerateFactory {
    @Autowired
    private ApplicationContext springContext;

    public ITextGenerate getGenerate(int mode) {
        switch (GenerateMode.getByCode(mode)) {
            case REMOTE_API:
                return springContext.getBean(OllamaDeepSeekTextGenerate.class);
            default:
                return springContext.getBean(TongYiTextGenerate.class);
        }
    }
}
