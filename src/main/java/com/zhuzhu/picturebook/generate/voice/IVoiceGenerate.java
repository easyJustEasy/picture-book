package com.zhuzhu.picturebook.generate.voice;

public interface IVoiceGenerate {
    String generate(String text, String voice,Float speed,String filePath) throws Exception;
}
