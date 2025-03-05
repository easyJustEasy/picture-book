package org.example.picturebook.util;

import cn.hutool.core.util.StrUtil;

public class DeepSeekUtil {
    public static String removeThink(String storyText) {
        if (StrUtil.isBlankIfStr(storyText)) {
            return storyText;
        }
        if (storyText.contains("<think>") && storyText.contains("</think>")) {
            storyText = storyText.substring(storyText.indexOf("</think>") + "</think>".length());
        }
        return storyText;
    }
}
