package org.example.picturebook.config;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.picturebook.dao.BookDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


import java.io.File;
import java.util.Objects;


@Component
@Slf4j
public class InitListener implements CommandLineRunner {
    @Autowired

    private AppConfig appConfig;


    @Override
    public void run(String... args) throws Exception {
        appConfig.dbInit();
        appConfig.tempDir();

        log.info("初始化完成");
    }

}

