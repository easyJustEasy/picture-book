package com.picture.book.dto;

import lombok.Data;

@Data
public class PictureDTO {
    //图片
    private String img;
    //字幕
    private String caption;
    //场景
    private String scene;
    //配音
    private String voice;
    //此图片生成的视频
    private String videoPath;

    public PictureDTO(String caption, String scene) {
        this.caption = caption;
        this.scene = scene;
    }
}
