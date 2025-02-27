package org.example.picturebook.generate.text;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.picturebook.util.DeepSeekUtil;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoteTextGenerate implements ITextGenerate {
    private static final String deepseekr17b = "deepseek-r1:7b";
    private static final String deepseekr114b = "deepseek-r1:14b";
    private static final String qwen2514b = "qwen2.5:14b";
    private static final String qwen2532b = "qwen2.5:32b";
    private static final String qwen2572b = "qwen2.5:72b";
    @Autowired
    private OllamaApi ollamaApi;

    @Override
    public String generate(String system, String prompt) throws Exception {
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder().model(deepseekr17b).temperature(0.6).build())
                .build();
        Prompt promptData = new Prompt(new SystemMessage(system), new UserMessage(prompt));


        ChatResponse response = chatModel.call(promptData);

        log.info("response:" + JSONObject.toJSONString(response));
        return DeepSeekUtil.removeThink(response.getResult().getOutput().getText());
    }
}
