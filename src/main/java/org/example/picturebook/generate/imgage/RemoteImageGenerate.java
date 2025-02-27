package org.example.picturebook.generate.imgage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoteImageGenerate extends AbstractImageGenerate implements IImageGenerate{
    @Override
    public String generate(String actors, String scene, String caption, String workDir) throws Exception {
        return null;
    }
}
