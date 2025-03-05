package org.example.picturebook.config;

import cn.hutool.core.util.StrUtil;
import org.example.picturebook.util.PropertiesUtil;


public class AppConfig {
    public static String apiKey;
    public static String tempDir;
    public static String videoDir;
    public static String videoUrl;

    public static synchronized String apiKey() throws Exception {
        String dashscopeApiKey = System.getenv("DASHSCOPE_API_KEY");
        if (StrUtil.isBlankIfStr(dashscopeApiKey)) {
            throw new RuntimeException("百炼APIKey为空！请先设置");
        }
        return dashscopeApiKey;
    }

    public static synchronized String tempDir() throws Exception {
        if (StrUtil.isBlankIfStr(tempDir)) {
            tempDir = PropertiesUtil.getProperty("app", "tempDir");
        }
        return tempDir;
    }

    public static synchronized String videoDir() throws Exception {
        if (StrUtil.isBlankIfStr(videoDir)) {
            videoDir = PropertiesUtil.getProperty("app", "videoDir");
        }
        return videoDir;
    }

    public static synchronized String videoUrl() throws Exception {
        if (StrUtil.isBlankIfStr(videoUrl)) {
            videoUrl = PropertiesUtil.getProperty("app", "videoUrl");
        }
        return videoUrl;
    }

    public static void main(String[] args) {
        try {
            System.out.println(apiKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
