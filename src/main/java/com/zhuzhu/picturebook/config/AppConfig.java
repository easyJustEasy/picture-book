package com.zhuzhu.picturebook.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zhuzhu.picturebook.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Component
public class AppConfig {
    public static String apiKey;
    public static String tempDir;
    public static String videoDir;
    public static String videoUrl;
    @Value("${local.user.home}")
    private String home;
    @Value("classpath:book.db")
    private Resource resource;

    public static synchronized String apiKey() throws Exception {
        String dashscopeApiKey = System.getenv("DASHSCOPE_API_KEY");
        if (StrUtil.isBlankIfStr(dashscopeApiKey)) {
            throw new RuntimeException("百炼APIKey为空！请先设置");
        }
        return dashscopeApiKey;
    }

    public synchronized String tempDir() throws Exception {
        File file = new File(home);
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        File temp = new File(file.getAbsolutePath() + File.separator + "temp");
        if (temp.exists()) {
            for (File listFile : Objects.requireNonNull(temp.listFiles())) {
                FileUtil.del(listFile);
            }
        } else {
            FileUtil.mkdir(temp);
        }
        return temp.getAbsolutePath();
    }

    public synchronized void dbInit() throws IOException {
        File file = new File(home);
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        //把数据库文件移动到home目录下
        File dbFile = new File(file.getAbsolutePath() + File.separator + "book.db");
        if (!dbFile.exists()) {
            FileUtil.copy(resource.getFile(), dbFile, true);
//            bookDao.initDb();
        }
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
