package com.chair.chairdada.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);

    // 保存所有在线的客户端会话
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    /**
     * 连接建立成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("有新连接加入！当前在线人数为: {}", sessions.size());
        broadcast("系统消息: 新用户加入聊天室，当前在线人数: " + sessions.size());
    }

    /**
     * 收到客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到来自客户端的消息: {}", payload);
        broadcast(payload);
    }

    /**
     * 连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("有一连接关闭！当前在线人数为: {}", sessions.size());
        broadcast("系统消息: 有用户离开聊天室，当前在线人数: " + sessions.size());
    }

    /**
     * 发生错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 发生错误", exception);
        if (session.isOpen()) {
            session.close();
        }
        sessions.remove(session);
    }

    /**
     * 广播消息
     */
    private void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                }
            }
        }
    }
}
