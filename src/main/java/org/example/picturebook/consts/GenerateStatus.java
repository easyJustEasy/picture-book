package org.example.picturebook.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GenerateStatus {
    txt_ing(1,"故事生成中"),
    txt_success(2,"故事生成成功"),
    txt_fail(3,"故事生成失败"),
    video_ing(4,"视频生成中"),
    video_success(5,"视频生成成功"),
    video_fail(6,"视频生成失败"),

    success(10,"成功"),
    fail(11,"失败"),
    ;
    private final int code;
    private final String desc;
}
