package org.example.picturebook.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {
    //创建一个线程池
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            50,
            100,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    //使用线程池执行任务
    public static void execute(Runnable runnable){
        threadPoolExecutor.execute(runnable);
    }
}
