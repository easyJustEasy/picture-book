package com.zhuzhu.picturebook.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class InitListener implements CommandLineRunner {
    @Autowired

    private AppConfig appConfig;


    @Override
    public void run(String... args) throws Exception {
        appConfig.dbInit();
        appConfig.tempDir(true);

        log.info("初始化完成");
    }

}

