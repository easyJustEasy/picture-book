package org.example.picturebook.generate.voice;

public interface IVoiceGenerate {
    String generate(String text, String voice,String workDir) throws Exception;
}
