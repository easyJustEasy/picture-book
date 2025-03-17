package org.example.picturebook.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SplitTextFileUtil {
    private static final int CHUNK_SIZE = 500; // 每个分割块的大小
       private static String parentDir = "E:\\work\\picture-book\\src\\test\\resources\\kongbu";

    public static void main(String[] args) throws IOException {
        File inputFile = new File(parentDir+File.separator+"1.txt"); // 输入文件路径
        splitFileBySentence(inputFile);
    }

    private static void splitFileBySentence(File file) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                contentBuilder.append(currentLine).append("\n");
            }
        }

        String content = contentBuilder.toString();
        String[] sentences = content.split("(?<=[。！？])"); // 使用正则表达式根据句子结束符分割

        List<String> chunks = new ArrayList<>();
        StringBuilder chunkBuilder = new StringBuilder();

        for (String sentence : sentences) {
            if (chunkBuilder.length() + sentence.length() > CHUNK_SIZE) {
                chunks.add(chunkBuilder.toString());
                chunkBuilder = new StringBuilder(sentence);
            } else {
                chunkBuilder.append(sentence);
            }
        }

        // 添加最后一个块
        if (chunkBuilder.length() > 0) {
            chunks.add(chunkBuilder.toString());
        }

        // 写入文件
        for (int i = 0; i < chunks.size(); i++) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(parentDir+File.separator+"gushi"+File.separator+i+"_output_part" + ".txt"))) {
                writer.print(chunks.get(i));
            }
        }
    }
}