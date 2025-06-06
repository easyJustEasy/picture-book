package com.zhuzhu.picturebook.dto;


import cn.hutool.core.util.StrUtil;
import lombok.Data;
import com.zhuzhu.picturebook.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
public class Story {
    private String title;
    private List<Scene> scenes;
    private String storySystemMessage;
    private String storyUserMessage;
    private String storyOutputMessage;

    @Data
    public static class Scene {
        private String sceneDesc;
        private String sceneTitle;
        //字幕
        private String caption;

        public static Scene parseScene(String text) {
            if (StrUtil.isBlankIfStr(text) || text.contains("故事标题：")) {
                return null;
            }
            text = StringUtil.replaceLineSeparator(text);
            Scene scene = new Scene();
            // 提取旁白
            String narration = extractPart(text, "旁白：", "场景描述：");
            // 提取场景描述
            String sceneDescription = extractPart(text, "场景描述：", StringUtil.LINE_SEPARATOR);
            if (StrUtil.isBlankIfStr(sceneDescription) && StrUtil.isBlankIfStr(narration)) {
                return null;
            }
            if (StrUtil.isNotBlank(narration)) {
                scene.setCaption(narration);
            }
            if (StrUtil.isNotBlank(sceneDescription)) {
                scene.setSceneDesc(sceneDescription);
            }
            return scene;
        }

        /**
         * 根据指定的标记从文本中提取部分内容。
         *
         * @param text        要处理的文本
         * @param startMarker 开始提取内容的标记
         * @param endMarker   结束提取内容的标记
         * @return 提取出的内容
         */
        private static String extractPart(String text, String startMarker, String endMarker) {
            int i = text.indexOf(startMarker);
            if (i<0) {
                return text;
            }
            int startIndex =  i+ startMarker.length();
            int endIndex = text.indexOf(endMarker, startIndex); // 查找下一个换行符作为结束位置
            if (endIndex == -1) { // 如果没有找到换行符，则取到最后
                endIndex = text.length();
            }
            return StringUtil.replaceLineSeparatorToBlank(text.substring(startIndex, endIndex).trim());
        }
    }

    public static Story parseStory(String storyText) {
        Story story = new Story();
        story.setStoryOutputMessage(storyText);
        story.setScenes(new ArrayList<>());
        story.setTitle(UUID.randomUUID().toString());
        if (!storyText.contains("故事标题：")) {
            return story;
        }
        if (storyText.contains("<think>") && storyText.contains("</think>")) {
            storyText = storyText.substring(storyText.indexOf("</think>") + "</think>".length());
        }
        List<String> list = Arrays.stream(storyText.split("@@@@@")).filter(StrUtil::isNotBlank).toList();
        for (String s : list) {
            if (s.contains("故事标题：")) {
                story.setTitle(s.replace("故事标题：", "").trim());
                continue;
            }
            Scene scene = Scene.parseScene(s);
            if (scene != null) {
                story.getScenes().add(scene);
            }
        }
        Scene scene = new Scene();
        scene.setSceneTitle("点点关注");
        scene.setCaption("点点关注，明天中大奖呦");
        scene.setSceneDesc("中大奖，有好运");
        story.getScenes().add(scene);
        return story;
    }
}
