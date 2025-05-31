package com.picture.mq;

import com.alibaba.fastjson.JSONObject;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.config.MqConfig;
import com.zhuzhu.picturebook.dto.GenImageDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = PictureBookApp.class)
public class MqTest {
@Autowired
    private RabbitMessagingTemplate template;
@Test
public void test(){
    for (int i = 0; i < 1000; i++) {
        GenImageDTO dto = new GenImageDTO();
        dto.setPrompt("一个美女"+i);
        dto.setStep(20);
        dto.setBatchNo("dddddd");

        template.convertAndSend(MqConfig.topicExchangeName,MqConfig.ROUTING_KEY_GEN, JSONObject.toJSONString(dto));
    }

}
}
