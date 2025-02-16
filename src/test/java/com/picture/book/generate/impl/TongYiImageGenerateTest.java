package com.picture.book.generate.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class TongYiImageGenerateTest {
    @Autowired
    private TongYiImageGenerate tongYiImageGenerate;
    @Test
    public void testGenerate() throws Exception {
        String actors = "";
        String scene = "海绵宝宝的画室里挤满了前来参观的朋友们，每个人都拿着笔记本记录下自己最喜欢的画，有的朋友甚至开始模仿起画中的图案。";
        String caption = "画展当天，所有的动物朋友都来了，他们对每幅画赞不绝口。  ";
        String result = tongYiImageGenerate.generate(actors, scene, caption);
        System.out.println(result);
    }

}