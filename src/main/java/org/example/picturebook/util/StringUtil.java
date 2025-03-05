package org.example.picturebook.util;

public class StringUtil {
    public static final  String LINE_SEPARATOR = "\n";
    public static  String replaceLineSeparator(String inputString){
        return inputString.replaceAll("\r\n", LINE_SEPARATOR)
                .replaceAll("\n\n",LINE_SEPARATOR);

    }
    public static  String replaceLineSeparatorToBlank(String inputString){
        return inputString.replaceAll("\\\\n", "");

    }
    public static void main(String[] args) {
//        System.out.println(replaceLineSeparator("我很好\r\n是的"));
//        System.out.println(replaceLineSeparator("我很好\n\n是的"));
//        System.out.println(replaceLineSeparator("我很好\\r\\n是的"));
        System.out.println(replaceLineSeparator("突然, 一个神秘的气泡升到了空中, 它上面写着 \\\"Swim with me!\\\"  \\n"));
    }
}
