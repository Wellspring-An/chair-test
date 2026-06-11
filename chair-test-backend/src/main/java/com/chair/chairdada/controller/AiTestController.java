package com.chair.chairdada.controller;


import com.chair.chairdada.bigmodel.AiChatManager;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

@RestController
public class AiTestController {

    @Resource
    private AiChatManager aiChatManager;

    @PostMapping("/Ai/test")
    public String test(@RequestParam("userMessage") String userMessage, @RequestParam("systemMessage") String systemMessage) {
        return aiChatManager.AiChat(userMessage, systemMessage);
    }

    @PostMapping("/Ai/testStream")
    public Flux<ChatResponse> testStream(@RequestParam("userMessage") String userMessage, @RequestParam("systemMessage") String systemMessage) {
        return aiChatManager.AiChatStream(userMessage, systemMessage);
    }
}
