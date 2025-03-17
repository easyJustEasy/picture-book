package com.zhuzhu.picturebook.dto;

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
    //工作目录
    private String workDir;

    public PictureDTO(String caption, String scene,String workDir) {
        this.caption = caption;
        this.scene = scene;
        this.workDir = workDir;
    }
}
