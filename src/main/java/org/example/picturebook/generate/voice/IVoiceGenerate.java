package org.example.picturebook.generate.voice;

public interface IVoiceGenerate {
    String generate(String text, String voice,Float speed,String workDir) throws Exception;
}
