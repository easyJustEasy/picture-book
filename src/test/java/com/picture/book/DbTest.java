package com.picture.book;


import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.picture.book.dao.BookDao;
import com.picture.book.dto.GenerateResultDTO;
import com.picture.book.dto.PageResult;
import com.picture.book.dto.QueryByPageRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class DbTest {


    @Autowired
    JdbcTemplate jdbcTemplate;
@Autowired
    BookDao bookDao;
    @Test
    public void test() throws Exception {
        jdbcTemplate.execute("drop table book;");
        // 1、首先创建数据表
        String ddl = """
            CREATE TABLE `book` (
                id integer PRIMARY KEY autoincrement,
                role_name TEXT,
                    story_desc TEXT,
                    create_time default (datetime('now', 'localtime')),
                    video_url TEXT,
                    error TEXT,
                    batch_id TEXT,
                    user_id TEXT,
                    story_system_message TEXT,
                    story_user_message TEXT,
                    story_output_message TEXT,
                    status TEXT
            );
        """;

        this.jdbcTemplate.execute(ddl);

//        // 2、插入一条数据
//        int ret = this.jdbcTemplate.update("INSERT INTO `user` (`id`, `name`, `create_at`) VALUES (?, ?, ?);", new Object[] {1, "springdoc", LocalDateTime.now()});
//
//        log.info("插入数据：{}", ret);
//
//        // 3、检索一条数据
//        Map<String, Object> user = this.jdbcTemplate.queryForObject("SELECT * FROM `user` WHERE `id` = ?", new ColumnMapRowMapper(), 1L);
//
//        log.info("检索数据：{}", user);
    }
    @Test
    public void test2() throws Exception {
        GenerateResultDTO generateResultDTO = new GenerateResultDTO();
        generateResultDTO.setRole("小明");
        generateResultDTO.setStoryDesc("小明在玩手机");
        generateResultDTO.setCreateTime(new Date());
        generateResultDTO.setVideoUrl("http://www.baidu.com");
        generateResultDTO.setError("");

        bookDao.save(generateResultDTO,"123");
        generateResultDTO.setRole("更新小明");
        bookDao.update(generateResultDTO,"123");
        System.out.println(generateResultDTO.getId());
    }
    @Test
    public void test3() throws Exception {
        PageResult pageResult = bookDao.queryByPage(new QueryByPageRequestDTO());
        log.info(JSONObject.toJSONString(pageResult));
    }
}