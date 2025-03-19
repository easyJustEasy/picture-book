package com.picture.img;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ImageSort {
    public static void main(String[] args) {
        File file = new File("D:\\soft\\nginx-1.26.3\\html\\img");
        File target = new File("E:\\toutiaoimge");
        LocalDate today = LocalDate.now();
        int i = 0;
        int j = 1;
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            File targetSub = new File(target.getAbsolutePath() + File.separator + today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if (i++ >= 120) {
                i = 0;
                j++;
                today = today.plusDays(1L);
            }
            if (!targetSub.exists()) {
                targetSub.mkdir();
            }
            FileUtil.move(listFile, targetSub, true);
        }
    }
}
