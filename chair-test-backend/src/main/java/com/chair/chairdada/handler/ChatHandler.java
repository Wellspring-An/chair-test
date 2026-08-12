package com.chair.chairdada.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chair.chairdada.config.TokenConfig;
import com.chair.chairdada.model.entity.User;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatHandler extends TextWebSocketHandler {

    @Autowired
    private TokenConfig tokenConfig;

    private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);

    // 保存所有在线的客户端会话
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    /**
     * 连接建立成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        List<String> strings = ((StandardWebSocketSession) session).getNativeSession().getRequestParameterMap().get("chair-token");
        User userInfo = tokenConfig.getUserInfo(strings.get(0));
        session.getAttributes().put("userId", userInfo.getId());
        sessions.add(session);
        log.info("有新连接加入！当前在线人数为: {}", sessions.size());
        ReceiveWebSocketMessage receiveWebSocketMessage = new ReceiveWebSocketMessage();
        receiveWebSocketMessage.setSender("系统消息");
        receiveWebSocketMessage.setMessage("欢迎加入聊天室，当前在线人数: " + sessions.size());
        receiveWebSocketMessage.setReceiver(strings.get(0));
        broadcast(receiveWebSocketMessage);
    }

    /**
     * 收到客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        SendWebSocketMessage bean = JSONUtil.toBean(payload, SendWebSocketMessage.class);
        log.info("收到来自客户端的消息: {}", payload);
        if ("heartbeat".equals(bean.type)) {
            return;
        }
        broadcast(bean);
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
    private void broadcast(Object message) {
        ReceiveWebSocketMessage receiveWebSocketMessage = new ReceiveWebSocketMessage();
        BeanUtil.copyProperties(message, receiveWebSocketMessage);
        receiveWebSocketMessage.setType("received");

        // 1. 安全校验：防止 receiver 无效导致 NPE
        User userInfo = tokenConfig.getUserInfo(receiveWebSocketMessage.getReceiver());
        if (userInfo == null) {
            log.warn("接收者不存在或 token 无效: {}", receiveWebSocketMessage.getReceiver());
            return;
        }
        long receiverId = userInfo.getId();

        // 2. 遍历所有 session 进行精准投递
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }

            long userId = (long) session.getAttributes().get("userId");

            // 3. 核心逻辑：仅向目标接收者发送消息（私聊）
            if (userId == receiverId) {
                try {
                    // 4. 正确的加锁方式：锁住 session 对象本身，防止并发发送报错
                    synchronized (session) {
                        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(receiveWebSocketMessage)));
                    }
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                    sessions.remove(session); // 发送失败建议移除失效 session
                }
            } else if (StrUtil.equals("system", receiveWebSocketMessage.getType())) {
                try {
                    // 4. 正确的加锁方式：锁住 session 对象本身，防止并发发送报错
                    synchronized (session) {
                        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(receiveWebSocketMessage)));
                    }
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                    sessions.remove(session); // 发送失败建议移除失效 session
                }
            }
        }
    }


    @Data
    class SendWebSocketMessage {
        private String message;
        private String type;
        private String sender;
        private String receiver;
        private String time;
    }

    @Data
    class ReceiveWebSocketMessage {
        private String message;
        private String type;
        private String sender;
        private String receiver;
        private String time;
    }
}
