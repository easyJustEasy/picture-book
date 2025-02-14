package com.picture.book.generate.impl;

import com.alibaba.fastjson.JSONObject;
import com.picture.book.dto.Story;
import com.picture.book.generate.ITextGenerate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.picture.book.consts.BookConsts.tail;

@Component
@Slf4j
public class OllamaDeepSeekTextGenerate implements ITextGenerate {
    @Autowired
    OllamaApi ollamaApi;

    @Override
    public Story generate(String system, String prompt) throws Exception {
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder().model("deepseek-r1:1.5b").temperature(0.6).build())
                .build();
        Prompt promptData = new Prompt(new SystemMessage(system), new UserMessage(prompt));

        ChatResponse response = chatModel.call(promptData);
        log.info("response:" + JSONObject.toJSONString(response));
        return Story.parseStory(response.getResult().getOutput().getContent());
    }
}
