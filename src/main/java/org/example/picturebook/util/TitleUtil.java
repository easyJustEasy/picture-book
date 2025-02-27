package org.example.picturebook.util;

import cn.hutool.core.util.StrUtil;

public class TitleUtil {
    private static final String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";

    public static String sub(String title) {
        if (StrUtil.isBlankIfStr(title)) {
            return StrUtil.EMPTY;
        }
        return StrUtil.sub(title.replaceAll(regEx, ""), 0, 20);
    }
}
