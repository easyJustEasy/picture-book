package com.picture.db;

import com.alibaba.fastjson.JSONObject;
import org.example.PictureBookApp;
import org.example.picturebook.dao.BookDao;
import org.example.picturebook.dto.GenerateResultDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PictureBookApp.class)
public class DbTest {
    @Autowired
    private BookDao bookDao;
    @Test
    void textGen() throws Exception {
        GenerateResultDTO generateResultDTO = bookDao.queryById(1);
        System.out.println(JSONObject.toJSONString(generateResultDTO));
        generateResultDTO.setStoryDesc("测试修改");
        bookDao.save(generateResultDTO,"12445");

    }
}
