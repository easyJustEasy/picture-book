package com.zhuzhu.picturebook.util;

import java.io.File;

public class DiskUtil {
    public static long getFreeSpace(File file) {
        File root = getRootDirectory(file);
        return transformation(root.getFreeSpace());

    }

    /**
     * 获取某个文件夹所在的根目录
     *
     * @param file
     * @return
     */
    public static File getRootDirectory(File file) {
        File parent = file.getParentFile();
        if (parent == null) {
            return file; // 当前已是根目录
        }
        return getRootDirectory(parent);
    }

    /**
     * 将字节容量转化为GB
     */
    public static long transformation(long size) {
        return size / 1024 / 1024 / 1024;
    }
}
