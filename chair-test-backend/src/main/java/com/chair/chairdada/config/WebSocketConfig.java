package com.chair.chairdada.config;

import com.chair.chairdada.handler.ChatHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 将 ChatHandler 绑定到 /chat 路径，并允许跨域
        registry.addHandler(new ChatHandler(), "/web/chat").setAllowedOrigins("*");
    }
}
