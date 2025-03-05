package org.example.picturebook.generate.text;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import lombok.extern.slf4j.Slf4j;
import org.example.picturebook.dto.Story;
import org.example.picturebook.config.AppConfig;
import org.springframework.stereotype.Component;

import java.util.Arrays;
@Slf4j
@Component
public class TongYiTextGenerate implements ITextGenerate{


    public  String generate(String system, String prompt) throws Exception {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(system)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(AppConfig.apiKey())
                .model("qwen-turbo")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        GenerationOutput output = gen.call(param).getOutput();
        String content = output.getChoices().get(0).getMessage().getContent();
        log.info("content:"+content);
        return content;

    }


}
