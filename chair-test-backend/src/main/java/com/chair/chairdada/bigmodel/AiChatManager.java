package com.chair.chairdada.bigmodel;


import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.List;

@Configuration
public class AiChatManager {

    @Resource
    private DeepSeekChatModel deepSeekChatModel;

    public String AiChat(String userMessageStr, String systemMessageStr) {
        UserMessage userMessage = new UserMessage(userMessageStr);
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);
        AssistantMessage assistantMessage = new AssistantMessage("```json\\n");
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage, assistantMessage),
                ChatOptions.builder().temperature(0.1).model("deepseek-v4-flash").stopSequences(List.of("```")).build());
        ChatResponse response = deepSeekChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    public Flux<ChatResponse> AiChatStream(String userMessageStr, String systemMessageStr) {
        UserMessage userMessage = new UserMessage(userMessageStr);
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);
        AssistantMessage assistantMessage = new AssistantMessage("```json\\n");
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage, assistantMessage),
                ChatOptions.builder().temperature(0.1).model("deepseek-v4-flash").stopSequences(List.of("```")).build());
        return deepSeekChatModel.stream(prompt);
    }
}
