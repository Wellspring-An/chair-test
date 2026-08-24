package com.chair.chairdada.config;

import com.chair.chairdada.model.entity.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatMqSender {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private ObjectMapper objectMapper;

    public void broadcastToAllInstance(WebSocketMessage webSocketMessage) {
        try {
            String json = objectMapper.writeValueAsString(webSocketMessage);
            // fanout交换机不需要routingKey，传null即可
            rabbitTemplate.convertAndSend(FanoutConfig.CHAT_FANOUT_EXCHANGE, "", json);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert WebSocketMessage to JSON: {}", webSocketMessage);
            e.printStackTrace();
        }
    }
}
