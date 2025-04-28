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
    public void test() {
        String sewd = "开封圣桦城附近的蛋糕店有哪些,热卖的产品有哪些，价格是多少";
        String wd = URLEncoder.encode(sewd, StandardCharsets.UTF_8);
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("https://www.baidu.com/s?wd=" + wd);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String content = page.content();
            String generate = tongYiTextGenerate.generate(String.format("""
                    根据这个内容提取用户需要的信息：‘%s’
                    """, content), sewd);
            System.out.println(generate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test2() {
        String sewd = "根据这个内容生成直播售卖话术";
        try {
            String content = """
                    3747404984296276328	包邮一件代发福利品type-c数据线8A充电线120W超级快充数据线	3C数码配件	手机配件	手机数据线		普通商品	{}			3471823156371970	{1M/一条装:1米8A数据线TYPEC口买一送一2条装}	2	4.50	46000	0	0	https://haohuo.jinritemai.com/ecommerce/trade/detail/index.html?id=3747404984296276328&origin_type=604	
                                        
                    """;
            String generate = tongYiTextGenerate.generate(String.format("""
                    根据这个内容生成直播售卖话术，不得超过200字：‘%s’
                    """, content), sewd);
            System.out.println("done:" + generate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
