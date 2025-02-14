package com.picture.book;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.picture.book.consts.BookConsts;
import com.picture.book.dto.Story;
import com.picture.book.generate.impl.OllamaDeepSeekTextGenerate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TestOllama {
    @Autowired
    OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;
    @Test
    public void test() throws Exception {
        String systemMessage =  StrUtil.replace(BookConsts.system, "%s", "海绵宝宝");

        Story s = ollamaDeepSeekTextGenerate.generate(systemMessage, "海绵宝宝去游泳");
        System.out.println(JSONObject.toJSONString(s));
    }
}
