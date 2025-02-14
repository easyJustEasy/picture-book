package com.picture.book.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GenerateStatus {
    ing(1,"生成中"),
    success(2,"成功"),
    fail(3,"失败"),
    ;
    private final int code;
    private final String desc;
}
