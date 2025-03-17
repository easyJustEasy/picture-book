package com.zhuzhu;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class AppTest {

    @Value("${user.home}")
    private String home;

    public static void main(String[] args) throws Exception {
        System.out.println(System.getenv("user.home"));
    }

    @Test
    public void test() throws Exception {
       File file = new File("C:\\Users\\Administrator\\Desktop\\dangao\\img");
       int i = 1;
        for (File listFile : file.listFiles()) {
            FileUtil.rename(listFile,(i++)+".jpg",true);
        }
    }

    @Test
    public void tests() throws IOException {
        //初始化代码
        StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
        Configuration cfg = Configuration.defaultConfiguration();
        GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
        String s = FileUtil.readString("classpath:audiohtml/index.html", StandardCharsets.UTF_8);
//获取模板
        Template t = gt.getTemplate(s);
        List<AudioDTO> list = new ArrayList<>();
        File file = new File("E:\\work\\picture-book\\voice\\asset");
        for (File listFile : file.listFiles()) {
            AudioDTO audio = new AudioDTO();
            String name = listFile.getName();
            audio.setImg("./asset/"+name+"/img.webp");
            audio.setTxt(FileUtil.readString(listFile.getAbsolutePath()+File.separator+"prompt.txt",StandardCharsets.UTF_8));
            audio.setName(name);
            audio.setMp3("./asset/"+name+"/prompt.mp3");
            list.add(audio);
        }
        t.binding("audioList", list);
//渲染结果
        String str = t.render();
        System.out.println(str);
        FileUtil.writeString(str,new File("temp/index.html"),StandardCharsets.UTF_8);
    }
}
