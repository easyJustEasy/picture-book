package com.zhuzhu.picturebook.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhuzhu.picturebook.config.MqConfig;
import com.zhuzhu.picturebook.controller.ImageController;
import com.zhuzhu.picturebook.dto.GenImageDTO;
import com.zhuzhu.picturebook.dto.GenImageDoneDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class MqService {
    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    public void addImage(GenImageDTO dto) {
        rabbitMessagingTemplate.convertAndSend(MqConfig.topicExchangeName, MqConfig.ROUTING_KEY_GEN, JSONObject.toJSONString(dto));
    }

    @RabbitListener(queues = MqConfig.GEN_DONE_QUEUE_AME)
    public void receiveMessage(String message) {
        GenImageDoneDTO jsonObject = JSONObject.parseObject(message, GenImageDoneDTO.class);
        if (StrUtil.isNotBlank(jsonObject.getImg())) {
            File f = new File(ImageController.workDir + File.separator + UUID.randomUUID()+ ".png");
            try {
                FileUtil.touch(f);
            } catch (Exception e) {
                log.error("create file error " + ExceptionUtil.getMessage(e));
            }
            writeImgToFile(f, jsonObject.getImg());
            ImageController.taskCount.decrementAndGet();
        }
    }

    private void writeImgToFile(File f, String base64Image) {
        // 去除可能存在的MIME前缀如"data:image/png;base64,"
        if (base64Image.contains(",")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }

        try {
            // 解码Base64字符串
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 将字节写入到文件
            try (OutputStream stream = new FileOutputStream(f)) {
                stream.write(imageBytes);
            }

            log.info("图片已成功保存为 " + f.getAbsolutePath());
        } catch (Exception e) {
            log.error("发生错误: " + e.getMessage());
        }
    }
}
