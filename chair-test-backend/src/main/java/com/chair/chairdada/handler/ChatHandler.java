package com.chair.chairdada.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chair.chairdada.config.FanoutConfig;
import com.chair.chairdada.config.TokenConfig;
import com.chair.chairdada.model.entity.User;
import com.chair.chairdada.model.entity.WebSocketMessage;
import com.chair.chairdada.model.enums.ChatMessageEnums;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    @Resource
    private TokenConfig tokenConfig;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);

    private final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        StandardWebSocketSession standardSession = (StandardWebSocketSession) session;
        String token = standardSession.getNativeSession().getRequestParameterMap().get("chair-token").get(0);
        User userInfo = tokenConfig.getUserInfo(token);
        Long userId = userInfo.getId();
        session.getAttributes().put("userId", userId);
        userSessionMap.put(userId, session);

        log.info("ws连接建立，userId:{}, 当前实例在线人数:{}", userId, userSessionMap.size());

        WebSocketMessage sysMsg = new WebSocketMessage();
        sysMsg.setSender("系统消息");
        sysMsg.setMessage("欢迎加入聊天室，当前在线人数: " + userSessionMap.size());
        sysMsg.setReceiver(String.valueOf(userId));
        sysMsg.setType(ChatMessageEnums.SYSTEM_MESSAGE.getValue());
        sendMessageToLocalUser(userId, sysMsg);
    }

    /**
     * 对外接口：发送消息，自动区分本地用户 / 跨实例MQ广播
     * @param msg 消息实体，receiver为接收人userId
     */
    public void sendMessage(WebSocketMessage msg) {
        Long targetUserId = Long.parseLong(msg.getReceiver());
        // 判断当前本机是否存在该用户
        if (userSessionMap.containsKey(targetUserId)) {
            // 本地在线，直接websocket发送
            sendMessageToLocalUser(targetUserId, msg);
        } else {
            // 不在本机，交给fanout广播给所有服务实例
            try {
                String json = objectMapper.writeValueAsString(msg);
                rabbitTemplate.convertAndSend(FanoutConfig.CHAT_FANOUT_EXCHANGE, "", json);
            } catch (JsonProcessingException e) {
                log.error("MQ广播消息序列化失败", e);
            }
        }
    }

    /**
     * MQ消费者，queuesToDeclare 动态生成独占临时队列，不要占位符
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(exclusive = "true", autoDelete = "true"),
                    exchange = @Exchange(value = FanoutConfig.CHAT_FANOUT_EXCHANGE, type = "fanout")
            ),
            ackMode = "MANUAL"
    )
    public void onMqReceive(org.springframework.amqp.core.Message message, com.rabbitmq.client.Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String json = new String(message.getBody());

        try {
            if (StrUtil.isBlank(json)) {
                return;
            }
            WebSocketMessage msg = JSONUtil.toBean(json, WebSocketMessage.class);
            if (StrUtil.isBlank(msg.getReceiver())) {
                log.warn("MQ消息receiver字段为空，丢弃消息");
                return;
            }
            Long targetUserId = Long.parseLong(msg.getReceiver());
            if (userSessionMap.containsKey(targetUserId)) {
                sendMessageToLocalUser(targetUserId, msg);
            }
        } catch (Exception e) {
            log.error("MQ消费异常 json={}", json, e);
        } finally {
            // ✅ 无论任何return、异常，这里100%执行，一定ack
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception ex) {
                log.error("执行ack失败", ex);
            }
        }
    }



    /**
     * 收到客户端websocket消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到ws客户端消息:{}", payload);
        WebSocketMessage bean = JSONUtil.toBean(payload, WebSocketMessage.class);

        // 心跳包直接忽略
        if ("heartbeat".equals(bean.getType())) {
            return;
        }
        // 收到客户端消息，统一调用sendMessage自动判断本地/MQ跨机
        sendMessage(bean);
    }

    /**
     * 连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj != null) {
            Long userId = Long.valueOf(userIdObj.toString());
            userSessionMap.remove(userId);
            log.info("ws连接断开 userId:{}，当前实例在线:{}", userId, userSessionMap.size());
        }
    }

    /**
     * 传输异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("ws传输异常", exception);
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj != null) {
            Long userId = Long.valueOf(userIdObj.toString());
            userSessionMap.remove(userId);
        }
        try {
            if (session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (IOException e) {
            log.error("关闭session异常", e);
        }
    }

    /**
     * 给本机存在的用户发送消息
     */
    public void sendMessageToLocalUser(Long targetUserId, WebSocketMessage message) {
        WebSocketSession session = userSessionMap.get(targetUserId);
        if (session == null || !session.isOpen()) {
            log.warn("本地不存在该用户session userId={}", targetUserId);
            userSessionMap.remove(targetUserId);
            return;
        }
        try {
            synchronized (session) {
                String json = JSONUtil.toJsonStr(message);
                log.info("ws发送消息 userId={}，消息内容:{}", targetUserId, json);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("本地发送消息失败 userId={}", targetUserId, e);
            userSessionMap.remove(targetUserId);
        }
    }

    /**
     * 判断本实例是否存在该用户在线
     */
    public boolean hasLocalSession(Long userId) {
        return userSessionMap.containsKey(userId);
    }
}
