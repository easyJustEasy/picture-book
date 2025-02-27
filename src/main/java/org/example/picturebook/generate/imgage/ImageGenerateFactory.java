package org.example.picturebook.generate.imgage;

import org.example.picturebook.consts.GenerateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ImageGenerateFactory {
    @Autowired
    private ApplicationContext springContext;

    public IImageGenerate getGenerate(int mode) {
        switch (GenerateMode.getByCode(mode)) {
            case REMOTE_API:
                return springContext.getBean(RemoteImageGenerate.class);
            default:
                return springContext.getBean(TongYiImageGenerate.class);
        }
    }
}
