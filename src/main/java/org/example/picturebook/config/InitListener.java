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


@Component
@Slf4j
public class InitListener implements CommandLineRunner {
    @Value("${local.user.home}")
    private String home;
    @Value("classpath:book.db")
    private Resource resource;
    @Autowired
    BookDao bookDao;

    @Override
    public void run(String... args) throws Exception {
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
        log.info("初始化完成");
    }

}

