package com.zhuzhu.picturebook.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    private TextConfig text;
    private ImageConfig image;
    private VoiceConfig voice;

    @Data
    public static class TextConfig {
        private int mode;
        private String remoteUrl;
        private String ollamaUrl;
    }

    @Data
    public static class ImageConfig {
        private int mode;
        private String remoteUrl;
    }

    @Data
    public static class VoiceConfig {
        private int mode;
        private String remoteUrl;

    }


}
