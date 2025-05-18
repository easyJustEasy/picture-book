package com.zhuzhu.picturebook.util;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil {


    public static String getFileNameFromURL(String url) {
        try {
            // 创建一个URL对象
            URL urlObject = new URL(url);

            // 获取路径部分
            String path = urlObject.getPath();

            // 从路径中获取文件名
            // 使用lastIndexOf查找最后一个斜杠的位置
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < path.length() - 1) { // 确保斜杠后还有字符
                return path.substring(lastSlashIndex + 1);
            } else {
                // 如果没有找到合适的文件名，则返回空字符串或其他标识符
                return "";
            }
        } catch (MalformedURLException e) {
            // 处理不合法的URL
            System.out.println("Invalid URL: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        String url = "https://example.com/path/to/your/file.txt";
        String fileName = getFileNameFromURL(url);
        if (fileName != null) {
            System.out.println("Extracted File Name: " + fileName);
        } else {
            System.out.println("Failed to extract file name.");
        }
    }

}
