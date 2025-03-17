package com.zhuzhu.picturebook.util;

import java.io.File;
import java.util.UUID;

public class FileUtils {
    public static String getUuidFileName(String workDir, String subfix) {
        return String.format("%s%s%s",workDir, File.separator, UUID.randomUUID()+subfix);
    }
}
