package com.picture.book.schedule;

import cn.hutool.core.io.FileUtil;
import com.picture.book.config.AppConfig;
import com.picture.book.consts.BookConsts;
import com.picture.book.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

@Component
@Slf4j
public class ScheduleTask {
    @Scheduled(cron = "0 0 0/1 * * *")
    public void deleteLog(){
        log.info("del "+"log");
    }
    @Scheduled(cron = "0 0/1 * * * *")
    public void deleteTemp(){
        ThreadUtil.execute(()->{
            if(BookConsts.BACK_TASK_COUNT.get()==0){
                try {
                    File file = new File(AppConfig.tempDir());
                    for (File listFile : Objects.requireNonNull(file.listFiles())) {
                        FileUtil.del(new File(listFile.getAbsolutePath()));
                        log.info("del "+listFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }
}
