package org.example;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.picturebook.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;

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
        System.out.println(home);
    }
}
