package com.picture.search;

import cn.hutool.http.HttpUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.generate.text.TongYiTextGenerate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SpringBootTest(classes = PictureBookApp.class)
public class SearchAi {
    @Autowired
    private OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;
    @Autowired
    private TongYiTextGenerate tongYiTextGenerate;
    @Test
    public void test(){
        String sewd = "开封圣桦城附近的蛋糕店有哪些,热卖的产品有哪些，价格是多少";
       String wd = URLEncoder.encode(sewd, StandardCharsets.UTF_8);
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("https://www.baidu.com/s?wd="+wd);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String content = page.content();
            String generate = tongYiTextGenerate.generate(String.format("""
                    根据这个内容提取用户需要的信息：‘%s’
                    """,content),sewd );
            System.out.println(generate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
