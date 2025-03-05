package org.example.picturebook.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum GenerateMode {
    TONGYI(1, "通义"),
    REMOTE_API(2,"远程调用"),;
    private int code;
    private String desc;

    public static GenerateMode getByCode(int mode) {
        return Arrays.stream(values()).filter(e->e.getCode()==mode).findFirst().orElse(null);
    }
}
