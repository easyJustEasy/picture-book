package org.example.picturebook.generate.text;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;

import jakarta.annotation.PostConstruct;
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
import reactor.core.publisher.Flux;
@Component
@Slf4j
public class OllamaDeepSeekTextGenerate implements ITextGenerate {
    private static final String deepseekr17b="deepseek-r1:7b";
    private static final String deepseekr114b="deepseek-r1:14b";
    private static final String qwen2514b="qwen2.5:14b";
    private static final String qwen2532b="qwen2.5:32b";
    private static final String qwen2572b="qwen2.5:72b";
    private static final String currentModel = deepseekr17b;
    @Autowired
    OllamaApi ollamaApi;
    @PostConstruct
    private void init(){
//        OllamaApi.PullModelRequest request = new OllamaApi.PullModelRequest(currentModel);
//        Flux<OllamaApi.ProgressResponse> progressResponseFlux = ollamaApi.pullModel(request);
//        progressResponseFlux.subscribe(t->{
//            log.info("pulling model {}",t);
//        },error->{
//            log.error("pull model error ,model name is {} ,error is {}",currentModel, ExceptionUtil.stacktraceToString(error));
//            throw new RuntimeException(error);
//        },()->{
//            log.info("pull model end success for {}",currentModel);
//        });
    }

    @Override
    public String generate(String system, String prompt) throws Exception {
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder().model(currentModel).temperature(0.6).build())
                .build();
        Prompt promptData = new Prompt(new SystemMessage(system), new UserMessage(prompt));


        ChatResponse response = chatModel.call(promptData);

        log.info("response:" + JSONObject.toJSONString(response));
        return DeepSeekUtil.removeThink(response.getResult().getOutput().getText());
    }
}
